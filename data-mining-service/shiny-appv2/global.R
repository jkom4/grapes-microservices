# global.R

# --- Libraries Needed Globally or by Helpers ---
library(DBI)
# library(RMySQL) # No longer needed for decision DB
library(RMariaDB) # Still needed for Operational DB
library(mongolite) # Needed for Decision DB
library(shiny)

# Couleurs
main_color <- "#9b59b6"
accent_color <- "#d98880"


# --- Database Connection Helper Functions ---

#' Connect to Operational MySQL DB (BD_OPER_ACTIVITIES)
#' Fetches credentials from environment variables.
#' @return A DBIConnection object or NULL on failure.
get_oper_db_connection <- function() {
  user <- Sys.getenv("MYSQL_USER", "adminMasi")
  pwd <- Sys.getenv("MYSQL_PWD", "@adminMASI")
  host <- Sys.getenv("MYSQL_HOST", "localhost")
  dbname <- Sys.getenv("MYSQL_DB_ACTIVITIES", "BD_OPER_ACTIVITIES")
  tryCatch({
    con <- dbConnect(RMariaDB::MariaDB(), # Using RMariaDB now
                     user = user, password = pwd, dbname = dbname, host = host)
    message("Connecting to Operational DB (", dbname, ")... OK")
    return(con)
  }, error = function(e) { warning("GLOBAL: Failed to connect to Operational DB: ", e$message); return(NULL) })
}

# --- REMOVED get_decis_db_connection() ---

#' Connect to Decision Staging MongoDB (BD_DECIS_TEMP)
#' Fetches credentials from environment variables.
#' @param collection_name The name of the collection (default: 'analysis_submissions').
#' @return A mongolite::mongo connection object or NULL on failure.
get_decis_mongo_connection <- function(collection_name = "analysis_submissions") {
  # Use a separate URI if needed, otherwise default to main one
  uri <- Sys.getenv("DECIS_MONGO_URI", Sys.getenv("MONGO_URI", "mongodb://localhost:27017/"))
  dbname <- Sys.getenv("DECIS_MONGO_DB", "BD_DECIS_TEMP") # DB name for decision temp
  
  tryCatch({
    con <- mongolite::mongo(collection = collection_name, db = dbname, url = uri)
    message("Connecting to Decision MongoDB (", dbname, "/", collection_name, ")... OK")
    return(con)
  }, error = function(e) {
    warning("GLOBAL: Failed to connect to Decision MongoDB: ", e$message)
    return(NULL)
  })
}


# --- Other Potential Global Helpers ---
safe_db_disconnect <- function(con) { # Keep for operational DB
  if (!is.null(con) && inherits(con, "DBIConnection") && dbIsValid(con)) { dbDisconnect(con) }
}
# Note: Mongolite connections usually managed automatically by scope/garbage collection

message("global.R sourced.")