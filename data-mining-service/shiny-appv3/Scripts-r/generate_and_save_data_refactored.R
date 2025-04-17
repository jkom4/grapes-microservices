# generate_and_save_data_refactored.R

# --- 0. Load Libraries ---
# Ensure these are run and packages are installed FIRST
library(dplyr)
library(DBI)         # << REQUIRED FOR dbConnect, dbExecute etc.
library(lubridate)
library(RMariaDB)    # << REQUIRED FOR dbConnect with MariaDB driver
library(mongolite)
library(uuid)
`%||%` <- function(a, b) if (!is.null(a)) a else b

# --- 1. Configuration & Parameters ---
MYSQL_USER_LOCAL <- Sys.getenv("MYSQL_USER")
MYSQL_PWD_LOCAL <- Sys.getenv("MYSQL_PWD") # Use correct password from .Renviron
MYSQL_HOST_LOCAL <- Sys.getenv("MYSQL_HOST")
# --- FIX: More robust Port check ---
mysql_port_value <- Sys.getenv("MYSQL_PORT") # Read as string first
MYSQL_PORT_LOCAL <- suppressWarnings(as.integer(mysql_port_value)) # Suppress warning here
# Explicit stop if NA or non-positive
if(is.na(MYSQL_PORT_LOCAL) || MYSQL_PORT_LOCAL <= 0) {
    stop(paste0("MYSQL_PORT ('", mysql_port_value, "') could not be read or converted to a valid integer > 0. Check .Renviron."))
}
# --- End PORT Fix ---
MYSQL_DB_ACTIVITIES_NAME <- Sys.getenv("MYSQL_DB_ACTIVITIES")

MONGO_URI_LOCAL <- Sys.getenv("MONGO_URI")
MONGO_DB_AUTH_NAME <- Sys.getenv("MONGO_DB_AUTH")
# --- Generation Parameters ---
TOTAL_CLIENTS <- 2000
TOTAL_PRODUCTS <- 50
TOTAL_SERVICES <- 10
TOTAL_TRANSACTIONS <- 15000
TOTAL_SERVICE_USAGE_LOGS <- 5000
TOTAL_AUTH_LOGS <- 25000

# --- Allowed Values & Names Lists ---
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
noms_fruits <- c("Apple", "Banana", "Orange", "Strawberry", "Mango", "Pear", "Grape", "Kiwi", "Cherry", "Watermelon", "Melon", "Pineapple", "Lemon", "Raspberry", "Blueberry", "Peach", "Plum", "Pomegranate", "Lychee", "Apricot", "Tomato", "Cucumber", "Carrot", "Beetroot", "Avocado", "Bell Pepper", "Onion", "Garlic", "Ginger", "Radish", "Zucchini", "Sweet Potato", "Cabbage", "Spinach", "Lettuce", "Green Bean", "Pea", "Broccoli", "Turnip", "Fennel", "Celery", "Cauliflower", "Corn", "Endive", "Asparagus", "Fig", "Walnut", "Almond", "Hazelnut", "Chestnut", "Milk", "Cheese", "Yogurt", "Bread", "Croissant")

# --- Random Seed ---
set.seed(123)

# --- 2. Helper Functions ---
get_mysql_connection_local <- function() {
  # Ensure required variables exist before printing/connecting
   if (!exists("MYSQL_USER_LOCAL") || !exists("MYSQL_PWD_LOCAL") || !exists("MYSQL_HOST_LOCAL") || !exists("MYSQL_PORT_LOCAL") || !exists("MYSQL_DB_ACTIVITIES_NAME")) {
        message("ERROR: Database connection variables not defined in get_mysql_connection_local environment.")
        return(NULL)
    }

  message(paste("LOCAL SCRIPT Connecting MariaDB: User=", MYSQL_USER_LOCAL, "Host=", MYSQL_HOST_LOCAL, "Port=", MYSQL_PORT_LOCAL, "DB=", MYSQL_DB_ACTIVITIES_NAME))

  tryCatch({
    # Check required packages are loaded *before* calling dbConnect
    if (!requireNamespace("DBI", quietly = TRUE) || !requireNamespace("RMariaDB", quietly = TRUE)) {
        stop("Required packages 'DBI' and 'RMariaDB' are not loaded. Run library(DBI); library(RMariaDB).")
    }
    # Remove the shiny::req check
    # req(isNamespaceLoaded("DBI"), isNamespaceLoaded("RMariaDB")) # REMOVED

    dbConnect(RMariaDB::MariaDB(), # Use RMariaDB driver
              user = MYSQL_USER_LOCAL,
              password = MYSQL_PWD_LOCAL,
              dbname = MYSQL_DB_ACTIVITIES_NAME,
              host = MYSQL_HOST_LOCAL,
              port = MYSQL_PORT_LOCAL) # Use verified port

  }, error = function(e) {
    message("Failed to connect to local MariaDB/MySQL: ", e$message)
    # Check specifically for 'dbConnect' error if packages might be unloaded
     if(grepl("could not find function.*dbConnect", e$message)){
        message("--> Ensure DBI and RMariaDB packages are loaded using library() first!")
    }
    return(NULL)
  })
}
# Function to safely connect to MongoDB using LOCAL settings
get_mongo_connection_local <- function(collection_name) {
   message(paste("LOCAL SCRIPT Connecting MongoDB: URI=", MONGO_URI_LOCAL, "DB=", MONGO_DB_AUTH_NAME, "Coll=", collection_name))
  tryCatch({
    mongolite::mongo(collection = collection_name, db = MONGO_DB_AUTH_NAME, url = MONGO_URI_LOCAL)
  }, error = function(e) {
    message("Failed to connect to local MongoDB collection '", collection_name, "': ", e$message)
    return(NULL)
  })
}

# Function to save to MySQL/MariaDB (uses DBI connection)
save_to_mysql <- function(con, data, table_name, overwrite = FALSE) {
  if (!is.null(con) && inherits(con, "DBIConnection")) {
    tryCatch({
      # Using append = !overwrite is generally safer than just overwrite=TRUE/FALSE
      dbWriteTable(con, table_name, data, row.names = FALSE, append = !overwrite, overwrite = overwrite)
      message("Successfully ", ifelse(overwrite, "overwritten", "appended to"), " table: ", table_name)
    }, error = function(e) {
      message("Error writing to DB table '", table_name, "': ", e$message)
    })
  } else { message("DB connection is invalid. Cannot write table: ", table_name) }
}

# Function to save to MongoDB (uses mongolite connection)
save_to_mongodb <- function(mongo_con, data, collection_name) {
  if (!is.null(mongo_con) && inherits(mongo_con, "mongo")) {
    tryCatch({ mongo_con$insert(data) ; message("Inserted data into MongoDB collection: ", collection_name) },
             error = function(e) { message("Error inserting MongoDB '", collection_name, "': ", e$message) })
  } else { message("MongoDB connection invalid. Cannot insert into: ", collection_name) }
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

  # Use the LOCAL connection helpers
  mysql_con <- get_mysql_connection_local()
  # Mongo connection established per operation via get_mongo_connection_local

  # Use on.exit for MariaDB connection
  #on.exit({ if (!is.null(mysql_con)) safe_db_disconnect(mysql_con) })

  if (is.null(mysql_con)) { message("Cannot proceed without DB connection. Exiting."); return(invisible(NULL)) }

  # --- Clear Existing Data (Using DBExecute on valid connection) ---
  message("Clearing existing data from DB tables...")
  tryCatch({ dbExecute(mysql_con, "DELETE FROM service_usage") }, error=function(e) message("Note: Could not clear service_usage: ", e$message))
  tryCatch({ dbExecute(mysql_con, "DELETE FROM transactions") }, error=function(e) message("Note: Could not clear transactions: ", e$message))
  tryCatch({ dbExecute(mysql_con, "DELETE FROM services") }, error=function(e) message("Note: Could not clear services: ", e$message))
  tryCatch({ dbExecute(mysql_con, "DELETE FROM products") }, error=function(e) message("Note: Could not clear products: ", e$message))
  tryCatch({ dbExecute(mysql_con, "DELETE FROM clients") }, error=function(e) message("Note: Could not clear clients: ", e$message))
  # Reset AI counters (optional, can fail if tables dont exist)
  tryCatch({ dbExecute(mysql_con, "ALTER TABLE clients AUTO_INCREMENT = 1") }, error=function(e) NULL)
  tryCatch({ dbExecute(mysql_con, "ALTER TABLE products AUTO_INCREMENT = 1") }, error=function(e) NULL)
  # ... etc for other AI tables

  # --- Generate Master Data ---
  message("Generating master data...")
  clients_data <- generate_clients(TOTAL_CLIENTS)
  products_data <- generate_products(TOTAL_PRODUCTS)
  services_data <- generate_services(TOTAL_SERVICES)

  # --- Save Master Data (Append ONLY) ---
  message("Appending master data to DB...")
  # NOTE: save_to_mysql now defaults to append = TRUE, overwrite = FALSE
 save_to_mysql(mysql_con, clients_data, "clients")
save_to_mysql(mysql_con, products_data, "products")
save_to_mysql(mysql_con, services_data, "services")
# --- Récupérer les vrais IDs depuis la DB ---
client_ids_db <- dbGetQuery(mysql_con, "SELECT client_id FROM clients")$client_id
product_ids_db <- dbGetQuery(mysql_con, "SELECT product_id FROM products")$product_id
service_ids_db <- dbGetQuery(mysql_con, "SELECT service_id FROM services")$service_id

# --- Generate Event Data ---
message("Generating event data...")
if (length(service_ids_db) > 0 && length(client_ids_db) > 0) {
  transactions_data <- generate_transactions(TOTAL_TRANSACTIONS, client_ids_db, product_ids_db, service_ids_db)
  service_usage_data <- generate_service_usage(TOTAL_SERVICE_USAGE_LOGS, client_ids_db, service_ids_db)
} else {
  message("Skipping transaction/service_usage generation as no service IDs were generated.")
  transactions_data <- tibble() # Empty tibbles
  service_usage_data <- tibble()
}

# --- Mongo ---
auth_logs_data <- generate_authentication_logs(TOTAL_AUTH_LOGS, client_ids_db)

  # --- Save Event Data (Append ONLY) ---
  message("Appending event data...")
  if(nrow(transactions_data)>0) save_to_mysql(mysql_con, transactions_data, "transactions")
  if(nrow(service_usage_data)>0) save_to_mysql(mysql_con, service_usage_data, "service_usage")

  # --- MongoDB Handling ---
  mongo_auth_con <- get_mongo_connection_local("authentication_logs") # Use local helper
  if (!is.null(mongo_auth_con)) {
      mongo_op_result <- tryCatch({ mongo_auth_con$drop(); TRUE }, error = function(e){ FALSE }) # Drop first
      if (isTRUE(mongo_op_result)) { save_to_mongodb(mongo_auth_con, auth_logs_data, "authentication_logs") }
      # No disconnect needed
  }
  mongo_op_result <- tryCatch({
    mongo_auth_con$drop()
    message("MongoDB collection dropped successfully.")
    TRUE
  }, error = function(e){
    message("MongoDB collection drop failed: ", e$message)
    FALSE
  })
  if (isTRUE(mongo_op_result)) {
    message("Calling save_to_mongodb()...")
    save_to_mongodb(mongo_auth_con, auth_logs_data, "authentication_logs")
  }
  
  

  message("--- Data Generation and Saving Complete ---")
  invisible(TRUE)
}

# --- 5. Run the main function ---
if (sys.nframe() == 0) { # Only run main() if script is sourced directly
    main_result <- tryCatch({ main() },
                           error = function(e) { message("!#! FATAL ERROR in main(): ", e$message); return(FALSE) })
    if(isFALSE(main_result)) message("Main execution failed.") else message("Main execution completed.")
}

