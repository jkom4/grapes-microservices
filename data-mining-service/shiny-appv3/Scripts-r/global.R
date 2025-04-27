# global.R

# --- Libraries Needed Globally or by Helpers ---
library(DBI)
library(RMariaDB) # Using RMariaDB
library(mongolite)
library(shiny)

# Couleurs (Keep if used elsewhere)
# main_color <- "#9b59b6"
# accent_color <- "#d98880"

# --- Database Connection Helper Functions ---

#' Connect to Operational MySQL/MariaDB DB (BD_OPER_ACTIVITIES)
#' Fetches credentials from environment variables.
#' Uses 'mariadb-db' as hostname default (for docker), falls back to 'localhost'.
#' @return A DBIConnection object or NULL on failure.
get_oper_db_connection <- function() {
    user <- Sys.getenv("MYSQL_USER")
    pwd <- Sys.getenv("MYSQL_PWD")
    # *** CHANGE: Default hostname to the Docker service name ***
    host <- Sys.getenv("MYSQL_HOST") # Default to service name
    # If MYSQL_HOST env var is set (e.g. to 'localhost' locally), it will override.
    dbname <- Sys.getenv("MYSQL_DB_ACTIVITIES")

    message(paste("Attempting MariaDB connection: User=", user, "Host=", host, "DB=", dbname)) # More debug info

    tryCatch({
        con <- dbConnect(RMariaDB::MariaDB(),
                     user = user, password = pwd, dbname = dbname, host = host)
        message("Connecting to Operational DB (", dbname, ")... OK")
        return(con)
    }, error = function(e) {
        warning("GLOBAL: Failed to connect to Operational DB: ", e$message); return(NULL)
    })
}


#' Connect to Decision Staging MongoDB (BD_DECIS_TEMP)
#' Fetches credentials from environment variables.
#' Uses 'mongo-db' as hostname default (for docker), falls back to 'localhost'.
#' @param collection_name The name of the collection (default: 'analysis_submissions').
#' @return A mongolite::mongo connection object or NULL on failure.
get_decis_mongo_connection <- function(collection_name = "analysis_submissions") {
    # *** CHANGE: Default hostname in URI construction ***
    default_host <- "mongo-db" # Docker service name
    mongo_host <- Sys.getenv("MONGO_HOST", default_host) # Allow overriding host via env var

    # Construct URI using default or environment var, fallback to localhost if needed? More complex.
    # Safer: Assume MONGO_URI covers everything if set, otherwise build for Docker/local
    default_uri <- paste0("mongodb://", mongo_host, ":27017/") # Default URI using service name or localhost
    uri <- Sys.getenv("MONGO_URI") # Use full URI if set, else construct

    # Decision DB name
    dbname <- Sys.getenv("DECIS_MONGO_DB")

    message(paste("Attempting Decision MongoDB connection: URI=", uri, "DB=", dbname, "Coll=", collection_name)) # More debug info

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
safe_db_disconnect <- function(con) {
  if (!is.null(con) && inherits(con, "DBIConnection") && dbIsValid(con)) { dbDisconnect(con) }
}

message("global.R sourced.")