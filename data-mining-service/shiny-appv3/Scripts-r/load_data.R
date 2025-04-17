# load_data.R

# --- 0. Load Libraries ---
library(dplyr)
library(DBI)
library(RMySQL)     # Or RMariaDB
library(mongolite)
library(lubridate)  # For date/time manipulation
library(tidyr)      # For drop_na, potentially pivot_*
library(shiny)      # For showNotification

# --- 1. Configuration ---
# (Configuration remains the same)
MYSQL_USER <- Sys.getenv("MYSQL_USER", "adminMASI")
MYSQL_PWD <- Sys.getenv("MYSQL_PWD", "@adminMASI") # Check if '@' is correct, often just password
MYSQL_HOST <- Sys.getenv("MYSQL_HOST", "127.0.0.1")
MYSQL_DB_ACTIVITIES <- "BD_OPER_ACTIVITIES"
MONGO_URI <- Sys.getenv("MONGO_URI", "mongodb://localhost:27017/")
MONGO_DB_AUTH <- "BD_OPER_PROC_AUTH"
MONGO_COLLECTION_AUTH <- "authentication_logs"
SAMPLE_SIZE <- 1000

# --- 2. Database Loading Functions ---
# (load_mysql_data and load_mongo_data remain the same)
load_mysql_data <- function() {
  mysql_con <- NULL
  on.exit({ if (!is.null(mysql_con) && inherits(mysql_con, "DBIConnection") && dbIsValid(mysql_con)) dbDisconnect(mysql_con) })
  tryCatch({
    mysql_con <- dbConnect(MySQL(), user = MYSQL_USER, password = MYSQL_PWD, dbname = MYSQL_DB_ACTIVITIES, host = MYSQL_HOST)
    message("Successfully connected to MySQL.")
    required_tables <- c("clients", "products", "services", "transactions", "service_usage")
    existing_tables <- dbListTables(mysql_con)
    missing_tables <- setdiff(required_tables, existing_tables)
    if (length(missing_tables) > 0) stop(paste("Required MySQL tables not found:", paste(missing_tables, collapse=", ")))
    clients <- dbReadTable(mysql_con, "clients") %>% as_tibble()
    products <- dbReadTable(mysql_con, "products") %>% as_tibble()
    services <- dbReadTable(mysql_con, "services") %>% as_tibble()
    transactions <- dbReadTable(mysql_con, "transactions") %>% as_tibble()
    service_usage <- dbReadTable(mysql_con, "service_usage") %>% as_tibble()
    message("Successfully loaded MySQL tables.")
    return(list(clients = clients, products = products, services = services, transactions = transactions, service_usage = service_usage))
  }, error = function(e) { stop("Fatal Error loading data from MySQL: ", e$message) })
}
load_mongo_data <- function() {
  tryCatch({
    fields_to_select <- '{"_id": 0, "client_id": 1, "timestamp": 1, "authentication_method": 1, "status": 1, "application_type": 1}'
    mongo_con <- mongo(collection = MONGO_COLLECTION_AUTH, db = MONGO_DB_AUTH, url = MONGO_URI)
    coll_count <- mongo_con$count()
    if(coll_count == 0) { warning("MongoDB collection '", MONGO_COLLECTION_AUTH, "' is empty."); return(tibble()) }
    auth_logs <- mongo_con$find(fields = fields_to_select) %>% as_tibble()
    message("Successfully loaded MongoDB collection: ", MONGO_COLLECTION_AUTH)
    return(auth_logs)
  }, error = function(e) { warning("Failed to load data from MongoDB: ", e$message); return(tibble()) })
}


# --- 3. Feature Engineering Function ---
# (create_new_variables remains the same, but be aware it uses original names like 'age', 'is_local')
create_new_variables <- function(df) {
  message("Creating new variables...")
  df %>%
    mutate(
      # Date/Time Features
      month = if("transaction_timestamp" %in% names(.)) month(transaction_timestamp) else NA_integer_,
      wday = if("transaction_timestamp" %in% names(.)) wday(transaction_timestamp, label = TRUE, week_start = 1) else NA,
      hour = if("transaction_timestamp" %in% names(.)) hour(transaction_timestamp) else NA_integer_,
      # Client Features
      age_group = if("age" %in% names(.)) case_when(
        age < 18 ~ "0-17",
        age >= 18 & age < 35 ~ "18-34",
        age >= 35 & age < 60 ~ "35-59",
        age >= 60 ~ "60+",
        TRUE ~ "Unknown"
      ) else NA_character_,
      # Product Features
      is_local_purchase = if("is_local" %in% names(.)) case_when( # Assumes 'is_local' came from products
        is_local == 1 ~ TRUE,
        is_local == 0 ~ FALSE,
        TRUE ~ NA
      ) else NA,
      # Transaction Features
      amount_category = if("total_amount" %in% names(.)) cut(total_amount,
                                                             breaks = c(-Inf, 10, 50, 100, Inf),
                                                             labels = c("<=10", "11-50", "51-100", ">100"),
                                                             right = FALSE) else NA
    )
}


# --- 4. Main Loading and Preparation Function ---

load_and_prepare_data <- function() {
  
  # --- Load Raw Data ---
  message("Attempting to load data from databases...")
  mysql_data <- tryCatch(load_mysql_data(), error = function(e) {
    showNotification(paste("Fatal Error:", e$message), type = "error", duration = NULL)
    return(NULL)
  })
  if (is.null(mysql_data)) return(NULL)
  mongo_data <- load_mongo_data() # Returns empty tibble on failure/empty
  
  # --- Data Type Conversion & Initial Cleaning ---
  message("Performing initial type conversions...")
  # Ensure ALL cleaning/typing happens first before summaries or joins
  
  clients_clean <- mysql_data$clients %>%
    mutate(client_id = as.integer(client_id),
           prefere_produit_local = as.logical(prefere_produit_local),
           signup_date = ymd(signup_date, quiet=TRUE))
  
  products_clean <- mysql_data$products %>%
    mutate(product_id = as.integer(product_id),
           is_local = as.logical(is_local))
  
  transactions_clean <- mysql_data$transactions %>%
    mutate(client_id = as.integer(client_id),
           product_id = as.integer(product_id),
           service_id = as.integer(service_id)) %>%
    mutate(transaction_timestamp = ymd_hms(transaction_timestamp, quiet = TRUE)) %>%
    filter(!is.na(transaction_timestamp))
  
  services_clean <- mysql_data$services %>% # Use a distinct name
    mutate(service_id = as.integer(service_id))
  
  # *** Moved service_usage_clean definition here ***
  service_usage_clean <- mysql_data$service_usage %>%
    mutate(client_id = as.integer(client_id),
           service_id = as.integer(service_id)) %>%
    mutate(usage_timestamp = ymd_hms(usage_timestamp, quiet = TRUE)) %>%
    filter(!is.na(usage_timestamp))
  
  # --- Calculate Summaries (AFTER cleaning is done) ---
  
  # Auth Summary
  auth_method_summary <- NULL
  if (nrow(mongo_data) > 0 && "authentication_method" %in% colnames(mongo_data)) {
    message("Calculating auth method summary...")
    auth_method_summary <- mongo_data %>%
      count(authentication_method, sort = TRUE, name = "count") %>%
      filter(!is.na(authentication_method))
  } else {
    warning("Cannot calculate auth summary: mongo_data empty or missing 'authentication_method'.")
  }
  
  # Service Usage Summary (Now service_usage_clean exists)
  service_usage_summary <- NULL
  message("--- Debugging Service Usage Summary ---")
  service_usage_exists <- exists("service_usage_clean") && is.data.frame(service_usage_clean)
  services_exist <- exists("services_clean") && is.data.frame(services_clean) # Check cleaned version
  message("Debug Service Summary: service_usage_clean exists? ", service_usage_exists)
  message("Debug Service Summary: services_clean exists? ", services_exist)
  
  if(service_usage_exists && services_exist) {
    service_usage_rows <- nrow(service_usage_clean)
    services_rows <- nrow(services_clean) # Check cleaned version
    message("Debug Service Summary: service_usage_clean rows: ", service_usage_rows)
    message("Debug Service Summary: services_clean rows: ", services_rows)
    usage_cols_ok <- all(c("service_id", "client_id") %in% colnames(service_usage_clean))
    services_cols_ok <- all(c("service_id", "name") %in% colnames(services_clean)) # Check name exists in services_clean
    message("Debug Service Summary: service_usage_clean required columns ok? ", usage_cols_ok)
    message("Debug Service Summary: services_clean required columns ok? ", services_cols_ok)
    
    if(service_usage_rows > 0 && services_rows > 0 && usage_cols_ok && services_cols_ok) {
      # Type conversion for join keys already done above
      service_usage_typed <- service_usage_clean
      services_typed <- services_clean
      
      message("Debug Service Summary: Types of service_id being joined:")
      print(paste("service_usage_typed$service_id:", class(service_usage_typed$service_id)))
      print(paste("services_typed$service_id:", class(services_typed$service_id)))
      
      service_usage_summary <- tryCatch({
        service_usage_typed %>%
          inner_join(services_typed %>% select(service_id, service_name = name), # Use name from services_clean
                     by = "service_id") %>%
          count(service_name, sort = TRUE, name = "count") %>%
          slice_head(n = 10)
      }, error = function(e){ warning("Error occurred during service usage summary calculation: ", e$message); return(NULL) })
      # ... (rest of summary check messages) ...
      if(!is.null(service_usage_summary) && nrow(service_usage_summary) > 0){ message("Debug Service Summary: Summary calculation successful.") } else if (!is.null(service_usage_summary) && nrow(service_usage_summary) == 0) { message("Debug Service Summary: Summary calculated but resulted in 0 rows.") } else { message("Debug Service Summary: Summary calculation failed or returned NULL.") }
      
    } else { warning("Could not generate service usage summary due to zero rows or missing columns.") }
  } else { warning("Could not generate service usage summary because prerequisite dataframes don't exist.") }
  message("--- End Debugging Service Usage Summary ---")
  
  
  # --- Enrich Transaction Data ---
  message("Enriching transaction data...")
  # (Transaction enrichment logic using *_clean dataframes remains the same)
  transactions_enriched <- tryCatch({
    transactions_clean %>%
      left_join(clients_clean, by = "client_id") %>%
      left_join(products_clean, by = "product_id") %>%
      left_join(services_clean, by = "service_id") # Join with cleaned services
  }, error = function(e){ message("!!! ERROR during join operations: ", e$message); showNotification("Error joining tables.", type="error", duration=NULL); return(NULL) })
  if(is.null(transactions_enriched)){ return(NULL) }
  
  
  # --- Enrich Auth Log Data (Optional) ---
  # (Auth log enrichment logic remains the same, using clients_clean)
  auth_logs_enriched <- tibble()
  if (nrow(mongo_data) > 0 && "client_id" %in% colnames(mongo_data)) {
    message("Enriching authentication log data...")
    auth_logs_temp <- mongo_data %>% mutate(client_id = as.integer(client_id))
    if(nrow(clients_clean) > 0 && "client_id" %in% colnames(clients_clean)) {
      auth_logs_enriched <- auth_logs_temp %>% left_join(clients_clean, by = "client_id")
    } else { warning("Cannot enrich auth logs: clients_clean missing."); auth_logs_enriched <- auth_logs_temp }
  }
  
  
  # --- Feature Engineering ---
  # (Feature engineering logic remains the same)
  transactions_enriched <- create_new_variables(transactions_enriched)
  
  
  # --- Debugging Output AFTER joins ---
  # (Debugging output remains the same)
  message("--- Debugging AFTER joins and feature engineering ---")
  message("Columns in transactions_enriched: ", paste(colnames(transactions_enriched), collapse=", "))
  message("--- End Debugging ---")
  
  
  # --- Sampling ---
  # (Sampling logic remains the same)
  message(paste("Sampling", SAMPLE_SIZE, "transactions..."))
  set.seed(123)
  n_rows_total <- nrow(transactions_enriched)
  actual_sample_size <- min(SAMPLE_SIZE, n_rows_total)
  if (actual_sample_size > 0) { sampled_data <- transactions_enriched %>% slice_sample(n = actual_sample_size) } else { showNotification("No transaction data available after enrichment.", type = "error", duration = NULL); return(NULL) }
  
  
  # --- Prepare Data for Specific Analyses (PCA/MCA) ---
  # (PCA/MCA logic remains the same, but CHECK potential_quali_cols again!)
  message("Preparing data subsets for PCA and MCA...")
  # Quantitative Data for PCA
  potential_quanti_cols <- c("age", "quantity", "unit_price", "total_amount", "delivery_time_days")
  actual_quanti_cols <- intersect(potential_quanti_cols, colnames(sampled_data))
  quantitative_data_clean <- NULL
  if(length(actual_quanti_cols) >= 2) {
    quantitative_data_raw <- sampled_data %>% select(all_of(actual_quanti_cols)) %>% mutate(across(everything(), as.numeric)) %>% select(where(is.numeric))
    quantitative_data_clean <- quantitative_data_raw %>% tidyr::drop_na()
    n_removed_na_q <- nrow(quantitative_data_raw) - nrow(quantitative_data_clean)
    if (n_removed_na_q > 0) message(paste("Removed", n_removed_na_q, "rows with NA from quantitative data for PCA."))
    if(nrow(quantitative_data_clean) < 2 || ncol(quantitative_data_clean) < 2){ warning("Insufficient data remaining for PCA after NA removal."); quantitative_data_clean <- NULL }
  } else { warning("Could not find at least two numeric columns for PCA.") }
  # Qualitative Data for MCA (Check Debug output for actual column names like name.x, category.x, name.y, category.y)
  potential_quali_cols <- c("region", "age_group", "payment_method", "saison", "category.x", "amount_category", "wday") # ADJUST THIS based on debug output
  actual_quali_cols <- intersect(potential_quali_cols, colnames(sampled_data))
  qualitative_data_clean <- NULL
  if(length(actual_quali_cols) >= 2) {
    qualitative_data_raw <- sampled_data %>% select(all_of(actual_quali_cols))
    qualitative_data_factors <- qualitative_data_raw %>% mutate(across(everything(), as.factor))
    qualitative_data_clean <- qualitative_data_factors %>% tidyr::drop_na()
    n_removed_na_ql <- nrow(qualitative_data_factors) - nrow(qualitative_data_clean)
    if (n_removed_na_ql > 0) message(paste("Removed", n_removed_na_ql, "rows with NA from qualitative data for MCA."))
    if(nrow(qualitative_data_clean) < 2 || ncol(qualitative_data_clean) < 2){ warning("Insufficient data remaining for MCA after NA removal."); qualitative_data_clean <- NULL }
  } else { warning("Could not find at least two categorical columns for MCA.") }
  
  
  message("Data loading and preparation complete.")
  
  # --- Prepare return list ---
  return_list <- list(
    full_sampled_data = sampled_data,
    quantitative_data = quantitative_data_clean, # Might be NULL
    qualitative_data = qualitative_data_clean,   # Might be NULL
    auth_summary = auth_method_summary,
    service_summary = service_usage_summary
  )
  
  # +++ Add Final Checks Before Return +++
  message("--- Checking Final List Structure Before Return ---")
  message("Is quantitative_data NULL? ", is.null(return_list$quantitative_data))
  if(!is.null(return_list$quantitative_data)) message("quantitative_data dims: ", paste(dim(return_list$quantitative_data), collapse="x"))
  message("Is qualitative_data NULL? ", is.null(return_list$qualitative_data))
  if(!is.null(return_list$qualitative_data)) message("qualitative_data dims: ", paste(dim(return_list$qualitative_data), collapse="x"))
  message("--- End Final Checks ---")
  
  return(return_list)
  
} # End load_and_prepare_data function