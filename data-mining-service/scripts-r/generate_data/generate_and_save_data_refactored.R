# generate_and_save_data_refactored.R

# --- 0. Load Libraries ---
# Ensure all necessary libraries are loaded first.
library(dplyr)
library(DBI)        # For database connections (used by RMySQL/RMariaDB)
library(lubridate)
library(RMySQL)     # Or RMariaDB if you use that driver
library(mongolite)
library(uuid)       # For generating unique IDs

# --- 1. Configuration & Parameters ---
# All configuration variables and allowed value vectors should be defined here.

# Database Credentials (FETCH FROM ENVIRONMENT VARIABLES)
MYSQL_USER <- Sys.getenv("MYSQL_USER", "adminMASI")
MYSQL_PWD <- Sys.getenv("MYSQL_PWD", "@adminMASI")
MYSQL_HOST <- Sys.getenv("MYSQL_HOST", "localhost")
MYSQL_DB_ACTIVITIES <- "BD_OPER_ACTIVITIES"

MONGO_URI <- Sys.getenv("MONGO_URI", "mongodb://localhost:27017/")
MONGO_DB_AUTH <- "BD_OPER_PROC_AUTH"


# Generation Parameters
TOTAL_CLIENTS <- 2000
TOTAL_PRODUCTS <- 50
TOTAL_SERVICES <- 10
TOTAL_TRANSACTIONS <- 15000
TOTAL_SERVICE_USAGE_LOGS <- 5000
TOTAL_AUTH_LOGS <- 25000

# --- Allowed Values & Names Lists (Crucial Definitions) ---

REGIONS <- c("Urban - High Density", "Urban - Medium Density", "Rural", "Unknown", "")
PROD_SAISONS <- c("Summer", "Winter", "Spring", "Autumn", "All Seasons", "Unknown")
PROD_CATEGORIES <- c("Fruits", "Vegetables", "Nuts", "Dairy", "Bakery")
SERVICE_CATEGORIES <- c("Information", "Support", "Financial", "Document")
SERVICE_NAMES <- c("BalanceCheck", "DocumentRequest", "ChatSupport", "LoanInfo", "StockUpdate", "PaymentAdvice", "PriceLookup", "ComplaintLog", "FAQAccess", "AccountStatement")
AUTHENTICATION_METHODS <- c("Password-Email", "EID", "SMS", "MASIID")
AUTH_STATUSES <- c("Success", "Failure")
APP_TYPES <- c("WebApp", "MobileApp", "API", "POS")
PAYMENT_METHODS <- c("Mastercard", "Visa")
PAYMENT_STATUSES <- c("Success", "Pending", "Failed")
DELIVERY_STATUSES <- c("Shipped", "Delivered", "Pending", "NotApplicable")
SOURCE_SYSTEMS <- c("BankA", "BankB", "InternalWebApp", "InternalMobileApp")

# List of realistic fruit/vegetable/item names for products (This was missing)
noms_fruits <- c("Apple", "Banana", "Orange", "Strawberry", "Mango", "Pear", "Grape", "Kiwi", "Cherry", "Watermelon",
                 "Melon", "Pineapple", "Lemon", "Raspberry", "Blueberry", "Peach", "Plum", "Pomegranate", "Lychee", "Apricot",
                 "Tomato", "Cucumber", "Carrot", "Beetroot", "Avocado", "Bell Pepper", "Onion", "Garlic", "Ginger", "Radish",
                 "Zucchini", "Sweet Potato", "Cabbage", "Spinach", "Lettuce", "Green Bean", "Pea", "Broccoli", "Turnip", "Fennel",
                 "Celery", "Cauliflower", "Corn", "Endive", "Asparagus", "Fig", "Walnut", "Almond", "Hazelnut", "Chestnut",
                 "Milk", "Cheese", "Yogurt", "Bread", "Croissant") # Added a few more diverse items


# --- Random Seed ---
set.seed(123) # for reproducibility


# --- 2. Helper Functions ---
# (These functions remain the same as before)

# Function to safely connect to MySQL
get_mysql_connection <- function() {
  tryCatch({
    dbConnect(RMySQL::MySQL(), # Explicitly namespace if needed, though library() should suffice
              user = MYSQL_USER,
              password = MYSQL_PWD,
              dbname = MYSQL_DB_ACTIVITIES,
              host = MYSQL_HOST)
  }, error = function(e) {
    message("Failed to connect to MySQL: ", e$message)
    return(NULL)
  })
}

# Function to safely connect to MongoDB
get_mongo_connection <- function(collection_name) {
  tryCatch({
    mongolite::mongo(collection = collection_name, db = MONGO_DB_AUTH, url = MONGO_URI)
  }, error = function(e) {
    message("Failed to connect to MongoDB collection '", collection_name, "': ", e$message)
    return(NULL)
  })
}

# Function to save to MySQL
save_to_mysql <- function(con, data, table_name, overwrite = FALSE) {
  # Check for valid connection object more robustly
  if (!is.null(con) && inherits(con, "DBIConnection")) {
    tryCatch({
      dbWriteTable(con, table_name, data, row.names = FALSE, overwrite = overwrite, append = !overwrite)
      message("Successfully ", ifelse(overwrite, "overwritten", "appended to"), " table: ", table_name)
    }, error = function(e) {
      message("Error writing to MySQL table '", table_name, "': ", e$message)
    })
  } else {
    message("MySQL connection is invalid or NULL. Cannot write table: ", table_name)
  }
}

# Function to save to MongoDB
save_to_mongodb <- function(mongo_con, data, collection_name) {
  if (!is.null(mongo_con) && inherits(mongo_con, "mongo")) {
    tryCatch({
      # Convert tibble/dataframe to list of lists for better BSON representation if needed
      # Though mongolite::insert usually handles dataframes well.
      mongo_con$insert(data)
      message("Successfully inserted data into MongoDB collection: ", collection_name)
    }, error = function(e) {
      message("Error inserting into MongoDB collection '", collection_name, "': ", e$message)
    })
  } else {
    message("MongoDB connection is invalid or NULL. Cannot insert into collection: ", collection_name)
  }
}

# --- 3. Data Generation Functions ---
# (These functions should now correctly find all config vectors defined above)

# Generate Clients (for BD_OPER_ACTIVITIES - MySQL)
generate_clients <- function(n) {
  tibble(
    region = sample(REGIONS, n, replace = TRUE, prob = c(0.45, 0.25, 0.15, 0.10, 0.05)),
    age = sample(c(NA, 5, 120, 18:85), n, replace = TRUE, prob = c(0.02, 0.01, 0.01, rep(0.96/(85-18+1), 85-18+1))),
    prefere_produit_local = sample(c(TRUE, FALSE, NA), n, replace = TRUE, prob = c(0.55, 0.35, 0.10)),
    signup_date = sample(seq(as.Date('2020-01-01'), Sys.Date() - 30, by="day"), n, replace = TRUE)
  ) %>%
    mutate(prefere_produit_local = as.integer(prefere_produit_local))
}

# Generate Products (for BD_OPER_ACTIVITIES - MySQL)
generate_products <- function(n) {
  tibble(
    name = sample(noms_fruits, n, replace = TRUE), # Should find noms_fruits now
    saison = sample(PROD_SAISONS, n, replace = TRUE, prob = c(0.2, 0.2, 0.1, 0.1, 0.3, 0.1)),
    is_local = sample(c(TRUE, FALSE, NA), n, replace = TRUE, prob = c(0.65, 0.25, 0.10)),
    base_price = round(runif(n, 0.5, 25), 2),
    category = sample(PROD_CATEGORIES, n, replace = TRUE)
  ) %>%
    mutate(is_local = as.integer(is_local))
}

# Generate Services (for BD_OPER_ACTIVITIES - MySQL)
generate_services <- function(n) {
  n_unique_names <- length(SERVICE_NAMES)
  if (n > n_unique_names) {
    message("Warning: Requesting more services (", n, ") than unique names available (", n_unique_names,"). Using ", n_unique_names, " services.")
    n <- n_unique_names
  }
  tibble(
    name = sample(SERVICE_NAMES, n, replace = FALSE),
    category = sample(SERVICE_CATEGORIES, n, replace = TRUE)
  )
}

# Generate Transactions (for BD_OPER_ACTIVITIES - MySQL)
generate_transactions <- function(n, client_ids, product_ids, service_ids) {
  trans_type <- sample(c("product", "service"), n, replace = TRUE, prob = c(0.95, 0.05))
  tibble(
    client_id = sample(client_ids, n, replace = TRUE),
    product_id = ifelse(trans_type == "product", sample(product_ids, n, replace = TRUE), NA_integer_), # Use typed NA
    service_id = ifelse(trans_type == "service", sample(service_ids, n, replace = TRUE), NA_integer_), # Use typed NA
    quantity = ifelse(trans_type == "product", sample(1:15, n, replace = TRUE), 1L), # Use integer literal
    unit_price = ifelse(trans_type == "product", round(runif(n, 0.4, 30), 2), round(runif(n, 5, 100), 2)),
    transaction_timestamp = sample(seq(as.POSIXct('2023-01-01 00:00:00'), Sys.time(), by="hour"), n, replace = TRUE),
    payment_method = sample(PAYMENT_METHODS, n, replace = TRUE),
    payment_status = sample(PAYMENT_STATUSES, n, replace = TRUE, prob = c(0.96, 0.02, 0.02)),
    delivery_status = ifelse(trans_type == "product", sample(DELIVERY_STATUSES, n, replace = TRUE), "NotApplicable"),
    delivery_time_days = ifelse(trans_type == "product" & delivery_status %in% c("Shipped", "Delivered"), sample(1:7, n, replace = TRUE), NA_integer_), # Typed NA
    source_system = sample(SOURCE_SYSTEMS, n, replace = TRUE)
  ) %>%
    mutate(total_amount = round(quantity * unit_price, 2)) %>%
    # Re-ensure mutual exclusivity - slightly safer logic
    mutate(
      product_id = ifelse(trans_type == "service", NA_integer_, product_id),
      service_id = ifelse(trans_type == "product", NA_integer_, service_id)
    ) %>%
    filter(
      !is.na(client_id) &
        ((!is.na(product_id) & is.na(service_id)) | (is.na(product_id) & !is.na(service_id)))
    ) %>%
    mutate(transaction_timestamp = as.POSIXct(transaction_timestamp)) # Ensure type
}

# Generate Service Usage Logs (for BD_OPER_ACTIVITIES - MySQL)
generate_service_usage <- function(n, client_ids, service_ids) {
  tibble(
    client_id = sample(client_ids, n, replace = TRUE),
    service_id = sample(service_ids, n, replace = TRUE),
    usage_timestamp = sample(seq(as.POSIXct('2023-01-01 00:00:00'), Sys.time(), by="min"), n, replace = TRUE),
    request_details = sample(c(NA_character_, "Request details example", "Specific query parameter"), n, replace=TRUE, prob=c(0.8, 0.1, 0.1)), # Typed NA
    status = sample(c("Completed", "Failed", "InProgress"), n, replace = TRUE, prob = c(0.95, 0.04, 0.01)),
    duration_ms = ifelse(status == "Completed", sample(50:5000, n, replace = TRUE), NA_integer_) # Typed NA
  ) %>%
    mutate(usage_timestamp = as.POSIXct(usage_timestamp)) # Ensure type
}

# Generate Authentication Logs (for BD_OPER_PROC_AUTH - MongoDB)
generate_authentication_logs <- function(n, client_ids) {
  start_date <- Sys.time() - years(1)
  end_date <- Sys.time()
  timestamps <- sample(seq(start_date, end_date, by = "sec"), n, replace = TRUE)
  tibble(
    auth_attempt_id = replicate(n, uuid::UUIDgenerate()),
    client_id = sample(client_ids, n, replace = TRUE),
    timestamp = timestamps,
    authentication_method = sample(AUTHENTICATION_METHODS, n, replace = TRUE, prob = c(0.5, 0.25, 0.15, 0.1)),
    status = sample(AUTH_STATUSES, n, replace = TRUE, prob = c(0.90, 0.10)),
    source_ip = replicate(n, paste(sample(0:255, 4, replace = TRUE), collapse = ".")),
    user_agent = sample(c("Mozilla/5.0 (Windows NT 10.0; Win64; x64)...", "Dalvik/2.1.0 (Linux; U; Android 11; SM-G975F)...", "APIClient/1.0", NA_character_), n, replace=TRUE, prob=c(0.5, 0.3, 0.1, 0.1)), # Typed NA
    application_type = sample(APP_TYPES, n, replace = TRUE, prob = c(0.5, 0.3, 0.1, 0.1)),
    failure_reason = ifelse(status == "Failure", sample(c("Invalid Credentials", "Account Locked", "Timeout", "Unknown Error"), n, replace = TRUE), NA_character_) # Typed NA
  )
}


# --- 4. Main Execution Function ---
main <- function() {
  message("--- Starting Data Generation and Saving ---")
  
  mysql_con <- NULL # Initialize to NULL
  on.exit({
    if (!is.null(mysql_con) && inherits(mysql_con, "DBIConnection") && dbIsValid(mysql_con)) {
      dbDisconnect(mysql_con)
      message("MySQL connection closed.")
    }
  })
  
  mysql_con <- get_mysql_connection() # Attempt connection
  
  if (is.null(mysql_con)) {
    message("Cannot proceed without MySQL connection. Exiting.")
    return(invisible(NULL)) # Exit gracefully
  }
  
  # --- Clear Existing Data (Instead of Overwriting Tables) ---
  # This ensures the table structure created manually remains intact
  message("Clearing existing data from MySQL master tables (if they exist)...")
  tryCatch({ dbExecute(mysql_con, "DELETE FROM clients") }, error=function(e) message("Note: Could not clear clients table (may not exist yet). ", e$message))
  tryCatch({ dbExecute(mysql_con, "DELETE FROM products") }, error=function(e) message("Note: Could not clear products table (may not exist yet). ", e$message))
  tryCatch({ dbExecute(mysql_con, "DELETE FROM services") }, error=function(e) message("Note: Could not clear services table (may not exist yet). ", e$message))
  # Reset AUTO_INCREMENT counters after deleting (optional but good practice for consistent IDs in testing)
  tryCatch({ dbExecute(mysql_con, "ALTER TABLE clients AUTO_INCREMENT = 1") }, error=function(e) message("Note: Could not reset AI for clients. ", e$message))
  tryCatch({ dbExecute(mysql_con, "ALTER TABLE products AUTO_INCREMENT = 1") }, error=function(e) message("Note: Could not reset AI for products. ", e$message))
  tryCatch({ dbExecute(mysql_con, "ALTER TABLE services AUTO_INCREMENT = 1") }, error=function(e) message("Note: Could not reset AI for services. ", e$message))
  
  message("Clearing existing data from MySQL event tables...")
  tryCatch({ dbExecute(mysql_con, "DELETE FROM transactions") }, error=function(e) message("Note: Could not clear transactions table. ", e$message))
  tryCatch({ dbExecute(mysql_con, "DELETE FROM service_usage") }, error=function(e) message("Note: Could not clear service_usage table. ", e$message))
  tryCatch({ dbExecute(mysql_con, "ALTER TABLE transactions AUTO_INCREMENT = 1") }, error=function(e) message("Note: Could not reset AI for transactions. ", e$message))
  tryCatch({ dbExecute(mysql_con, "ALTER TABLE service_usage AUTO_INCREMENT = 1") }, error=function(e) message("Note: Could not reset AI for service_usage. ", e$message))
  
  
  # --- Generate Master Data ---
  message("Generating master data...")
  clients_data <- generate_clients(TOTAL_CLIENTS)
  products_data <- generate_products(TOTAL_PRODUCTS)
  services_data <- generate_services(TOTAL_SERVICES)
  
  # --- Save Master Data (APPEND ONLY) ---
  # Now using append=TRUE (default), overwrite=FALSE
  message("Appending master data to MySQL (BD_OPER_ACTIVITIES)...")
  save_to_mysql(mysql_con, clients_data, "clients", overwrite = FALSE)
  save_to_mysql(mysql_con, products_data, "products", overwrite = FALSE)
  save_to_mysql(mysql_con, services_data, "services", overwrite = FALSE)
  
  # --- Retrieve/Simulate generated IDs ---
  # This assumption is now more reliable after resetting AUTO_INCREMENT
  client_ids_db <- 1:TOTAL_CLIENTS
  product_ids_db <- 1:TOTAL_PRODUCTS
  actual_total_services <- nrow(services_data)
  service_ids_db <- 1:actual_total_services
  
  # --- Generate Event Data ---
  message("Generating event data...")
  transactions_data <- generate_transactions(TOTAL_TRANSACTIONS, client_ids_db, product_ids_db, service_ids_db)
  service_usage_data <- generate_service_usage(TOTAL_SERVICE_USAGE_LOGS, client_ids_db, service_ids_db)
  auth_logs_data <- generate_authentication_logs(TOTAL_AUTH_LOGS, client_ids_db)
  
  # --- Save Event Data (APPEND ONLY) ---
  message("Appending event data...")
  save_to_mysql(mysql_con, transactions_data, "transactions", overwrite = FALSE)
  save_to_mysql(mysql_con, service_usage_data, "service_usage", overwrite = FALSE)
  
  # --- MongoDB Handling (Drop and Insert is fine here) ---
  mongo_auth_con <- get_mongo_connection("authentication_logs")
  if (!is.null(mongo_auth_con)) {
    mongo_op_result <- tryCatch({
      mongo_auth_con$drop()
      message("Dropped existing MongoDB collection: authentication_logs")
      TRUE
    }, error = function(e){
      message("Warning: Failed to drop MongoDB collection 'authentication_logs': ", e$message)
      FALSE
    })
    if (isTRUE(mongo_op_result)) {
      save_to_mongodb(mongo_auth_con, auth_logs_data, "authentication_logs")
    }
  }
  
  message("--- Data Generation and Saving Complete ---")
  invisible(TRUE)
}

# --- 5. Run the main function ---
# (Execution logic remains the same)
if (sys.nframe() == 0) {
  main_result <- tryCatch({
    main()
  }, error = function(e) {
    message("An error occurred during main execution: ", e$message)
    return(FALSE)
  })
}

