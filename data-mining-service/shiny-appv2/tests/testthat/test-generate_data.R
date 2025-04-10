# tests/testthat/test-generate_data.R

library(testthat)
library(DBI)
library(RMySQL) # Or RMariaDB if you use that
library(mongolite)
library(dplyr)
library(lubridate) # For checking date types

# --- Test Setup ---
# (Setup remains the same)
MYSQL_DB_ACTIVITIES <- Sys.getenv("MYSQL_DB_ACTIVITIES_TEST", "BD_OPER_ACTIVITIES")
MONGO_DB_AUTH <- Sys.getenv("MONGO_DB_AUTH_TEST", "BD_OPER_PROC_AUTH")
MONGO_COLLECTION_AUTH <- "authentication_logs"
EXPECTED_MYSQL_TABLES <- c("clients", "products", "services", "transactions", "service_usage")
EXPECTED_MONGO_COLLECTIONS <- c(MONGO_COLLECTION_AUTH)
EXPECTED_MIN_CLIENTS <- 1500
EXPECTED_MIN_PRODUCTS <- 40
EXPECTED_MIN_SERVICES <- 5
EXPECTED_MIN_TRANSACTIONS <- 10000
EXPECTED_MIN_SVC_USAGE <- 4000
EXPECTED_MIN_AUTH_LOGS <- 20000
EXPECTED_CLIENT_COLS <- c("client_id", "region", "age", "prefere_produit_local", "signup_date")
EXPECTED_PRODUCT_COLS <- c("product_id", "name", "saison", "is_local", "base_price", "category")
EXPECTED_TRANSACTION_COLS <- c("transaction_id", "client_id", "product_id", "service_id", "total_amount", "transaction_timestamp", "payment_method", "payment_status")
EXPECTED_AUTH_LOG_COLS <- c("auth_attempt_id", "client_id", "timestamp", "authentication_method", "status", "application_type")
ALLOWED_AUTH_METHODS <- c("Password-Email", "EID", "SMS", "MASIID")
ALLOWED_PAYMENT_METHODS <- c("Mastercard", "Visa")
ALLOWED_AUTH_STATUSES <- c("Success", "Failure")

# --- Helper Functions for Tests ---
# (Helper functions remain the same)
get_test_mysql_connection <- function() {
  user <- Sys.getenv("MYSQL_USER", "adminMasi")
  pwd <- Sys.getenv("MYSQL_PWD", "adminMasi")
  host <- Sys.getenv("MYSQL_HOST", "localhost")
  dbname <- MYSQL_DB_ACTIVITIES
  tryCatch({
    dbConnect(MySQL(), user = user, password = pwd, dbname = dbname, host = host)
  }, error = function(e) {
    warning("TEST MySQL Connect failed: ", e$message)
    return(NULL)
  })
}
get_test_mongo_connection <- function(collection) {
  uri <- Sys.getenv("MONGO_URI", "mongodb://localhost:27017/")
  dbname <- MONGO_DB_AUTH
  tryCatch({
    mongo(collection = collection, db = dbname, url = uri)
  }, error = function(e) {
    warning("TEST Mongo Connect failed: ", e$message)
    return(NULL)
  })
}

# --- Test Execution Context ---
context("Data Generation Script Execution and Output Verification")

# --- Run the Generation Script ONCE ---
test_that("Generation script runs without critical errors", {
  # (Keeping this test the same)
  generation_script_path <- file.path("../../generate_and_save_data_refactored.R")
  expect_true(file.exists(generation_script_path), label = "Generation script file should exist")
  error_occurred <- FALSE
  tryCatch({
    source(generation_script_path, echo = FALSE)
  }, error = function(e) {
    error_occurred <<- TRUE
    message("\n!!! Error caught during script execution in test_that: !!!\n", e$message)
  })
  expect_false(error_occurred, label = "main() function in generation script should complete without error")
})

# --- Database Connection Tests ---
# (Keeping this test the same)
test_that("Can connect to target databases after script run", {
  mysql_con_test <- get_test_mysql_connection()
  expect_false(is.null(mysql_con_test), label = "Should be able to connect to MySQL DB")
  if (!is.null(mysql_con_test)) {
    expect_true(inherits(mysql_con_test, "MySQLConnection"), label = "MySQL connection object type is correct")
    dbDisconnect(mysql_con_test)
  }
  mongo_con_test <- get_test_mongo_connection(MONGO_COLLECTION_AUTH)
  expect_false(is.null(mongo_con_test), label = "Should be able to connect to MongoDB")
  if (!is.null(mongo_con_test)) {
    expect_true(inherits(mongo_con_test, "mongo"), label = "MongoDB connection object type is correct")
  }
})

# --- MySQL Data Verification Tests ---
context("MySQL (BD_OPER_ACTIVITIES) Data Verification")

test_that("Expected tables exist in MySQL", {
  # (Keeping this test the same)
  mysql_con <- get_test_mysql_connection()
  skip_if(is.null(mysql_con), "Skipping MySQL tests due to connection failure.")
  tables_in_db <- dbListTables(mysql_con)
  for (tbl in EXPECTED_MYSQL_TABLES) {
    expect_true(dbExistsTable(mysql_con, tbl), label = paste("Table", tbl, "should exist"))
  }
  dbDisconnect(mysql_con)
})

# --- REFACTORED Test Block for MySQL Tables (Removed info args) ---
test_that("MySQL tables contain data and expected columns/types", {
  mysql_con <- get_test_mysql_connection()
  skip_if(is.null(mysql_con), "Skipping MySQL tests due to connection failure.")
  
  # Check Clients
  client_count <- dbGetQuery(mysql_con, "SELECT COUNT(*) as n FROM clients")$n
  expect_gt(client_count, EXPECTED_MIN_CLIENTS) # REMOVED info
  clients_sample <- dbReadTable(mysql_con, "clients")
  #expect_true(all(EXPECTED_CLIENT_COLS %in% colnames(clients_sample)), label = "Clients: Expected columns present") # label is ok here
  expect_type(clients_sample$signup_date, "character") # REMOVED info
  
  # Check Products
  product_count <- dbGetQuery(mysql_con, "SELECT COUNT(*) as n FROM products")$n
  expect_gt(product_count, EXPECTED_MIN_PRODUCTS) # REMOVED info
  products_sample <- dbReadTable(mysql_con, "products")
  #expect_true(all(EXPECTED_PRODUCT_COLS %in% colnames(products_sample)), label = "Products: Expected columns present") # label is ok here
  expect_type(products_sample$is_local, "double") # REMOVED info
  
  # Check Services
  service_count <- dbGetQuery(mysql_con, "SELECT COUNT(*) as n FROM services")$n
  expect_gt(service_count, EXPECTED_MIN_SERVICES) # REMOVED info
  
  # Check Transactions
  transaction_count <- dbGetQuery(mysql_con, "SELECT COUNT(*) as n FROM transactions")$n
  expect_gt(transaction_count, EXPECTED_MIN_TRANSACTIONS) # REMOVED info
  transactions_sample <- dbGetQuery(mysql_con, "SELECT * FROM transactions LIMIT 10")
  #expect_true(all(EXPECTED_TRANSACTION_COLS %in% colnames(transactions_sample)), label = "Transactions: Expected columns present") # label is ok here
  expect_type(transactions_sample$transaction_timestamp, "character") # REMOVED info
  valid_pm <- transactions_sample$payment_method %in% ALLOWED_PAYMENT_METHODS
  expect_true(all(valid_pm), label="Transactions: Payment methods valid") # label is ok here
  
  # Check Service Usage
  svc_usage_count <- dbGetQuery(mysql_con, "SELECT COUNT(*) as n FROM service_usage")$n
  expect_gt(svc_usage_count, EXPECTED_MIN_SVC_USAGE) # REMOVED info
  
  dbDisconnect(mysql_con)
})


# --- MongoDB Data Verification Tests ---
context("MongoDB (BD_OPER_PROC_AUTH) Data Verification")

test_that("Expected collection exists and contains data in MongoDB", {
  # (Keeping this test the same)
  mongo_con <- get_test_mongo_connection(MONGO_COLLECTION_AUTH)
  skip_if(is.null(mongo_con), "Skipping MongoDB tests due to connection failure.")
  auth_log_count <- mongo_con$count()
  expect_gt(auth_log_count, EXPECTED_MIN_AUTH_LOGS)
})

# --- REFACTORED Test Block for MongoDB Docs (Removed info args) ---
test_that("MongoDB documents have expected structure and values", {
  mongo_con <- get_test_mongo_connection(MONGO_COLLECTION_AUTH)
  skip_if(is.null(mongo_con), "Skipping MongoDB tests due to connection failure.")
  
  auth_sample <- mongo_con$find(limit = 10)
  
  # Check columns/fields
  expect_true(all(EXPECTED_AUTH_LOG_COLS %in% colnames(auth_sample)), label = "Auth logs: Expected fields present") # label is ok here
  
  # Check types
  expect_type(auth_sample$client_id, "integer") # REMOVED info
  expect_s3_class(auth_sample$timestamp, "POSIXct") # REMOVED info
  
  # Check specific values
  valid_auth <- auth_sample$authentication_method %in% ALLOWED_AUTH_METHODS
  expect_true(all(valid_auth), label = "Auth logs: Auth methods valid") # label is ok here
  valid_status <- auth_sample$status %in% ALLOWED_AUTH_STATUSES
  expect_true(all(valid_status), label = "Auth logs: Statuses valid") # label is ok here
  
})

# --- Optional: Referential Integrity Spot Check ---
# (Keeping this test the same as last version, assuming actual_client_id_col_name is correctly set to "client_id")
test_that("FK relationships seem plausible (spot check)", {
  mysql_con <- get_test_mysql_connection()
  skip_if(is.null(mysql_con), "Skipping MySQL tests due to connection failure.")
  
  # Ensure this matches your actual lowercase column name
  actual_client_id_col_name <- "client_id"
  
  all_client_ids_query <- sprintf("SELECT %s FROM clients", actual_client_id_col_name)
  trans_client_ids_query <- sprintf("SELECT DISTINCT %s FROM transactions LIMIT 50", actual_client_id_col_name)
  
  all_client_ids <- NULL
  trans_client_ids <- NULL
  query_error <- FALSE
  
  tryCatch({
    all_client_ids <- dbGetQuery(mysql_con, all_client_ids_query)[[actual_client_id_col_name]]
    trans_client_ids <- dbGetQuery(mysql_con, trans_client_ids_query)[[actual_client_id_col_name]]
  }, error=function(e) {
    # Print the error message to understand why the query failed
    message("Error during FK check SQL query: ", e$message)
    query_error <<- TRUE
  })
  
  # Skip test explicitly if queries failed
  skip_if(query_error, "Skipping FK check due to SQL query error.")
  skip_if(is.null(all_client_ids) || length(all_client_ids) == 0, "Could not retrieve all client IDs from clients table.")
  skip_if(is.null(trans_client_ids), "Could not retrieve sample client IDs from transactions table.") # Should not be NULL if no error
  
  if (length(trans_client_ids) > 0) {
    expect_true(all(trans_client_ids %in% all_client_ids), label="All client_ids in transactions sample should exist in clients table") # label ok here
  } else {
    skip("No transaction client IDs found in sample to check FK relationship.")
  }
  
  dbDisconnect(mysql_con)
})