# modules/mod_validation.R

# --- Libraries ---
library(shiny)
library(DT)
library(dplyr)
library(mongolite) # To interact with BD_DECIS_TEMP
library(shinycssloaders)
library(jsonlite) # To potentially parse key_results display

# Note: Depends on global.R for get_decis_mongo_connection

# --- UI Function ---
mod_validation_ui <- function(id) {
  ns <- NS(id)
  tagList(
    h2("Validation des Analyses Soumises"),
    p("Interface pour le Chief Data Scientist (simulé)."),
    hr(),
    actionButton(ns("refresh_submissions"), "Rafraîchir la Liste", icon = icon("sync")),
    hr(),
    h4("Analyses en Attente d'Approbation ('Pending Approval')"),
    withSpinner(DT::dataTableOutput(ns("pending_submissions_table")))
    # Potentially add another table later for Approved/Rejected history
  )
}


# --- Server Function ---
mod_validation_server <- function(id) {
  moduleServer(id, function(input, output, session) {
    ns <- session$ns
    
    # --- Reactive Value to Trigger Refreshes ---
    # Increment this value to force the table data to reload
    trigger_refresh <- reactiveVal(0)
    
    # --- Fetch Pending Submissions ---
    # eventReactive triggered by the refresh button OR the initial load trigger
    pending_data <- eventReactive(list(input$refresh_submissions, trigger_refresh()), {
      message("Validation Module: Fetching pending submissions...")
      con <- NULL # Initialize connection variable outside tryCatch if needed elsewhere
      tryCatch({
        con <- get_decis_mongo_connection() # Default collection is analysis_submissions
        req(con) # Need connection
        
        # Query for documents where status is 'Pending Approval'
        # Project only necessary fields for the table display + _id
        query <- '{"status": "Pending Approval"}'
        # Exclude potentially large 'key_results' from initial table view?
        # Or select specific fields from it later if parsing JSON
        fields <- '{"_id": 1, "timestamp": 1, "analysis_type": 1, "selected_vars": 1, "conclusion": 1, "analyst_id": 1}' # Include _id!
        
        data <- con$find(query = query, fields = fields)
        
        # Add buttons column - IMPORTANT: IDs need unique identifiers
        if (nrow(data) > 0) {
          # Make sure _id is character for use in input IDs
          data$`_id` <- as.character(data$`_id`)
          
          # Generate HTML for buttons in each row
          actions <- purrr::map_chr(data$`_id`, function(id_) {
            paste0(
              # Approve Button
              as.character(actionButton(inputId = ns(paste0("approve_", id_)),
                                        label = "Approuver",
                                        icon = icon("check"),
                                        class = "btn-success btn-sm", # Styling
                                        onclick = sprintf("Shiny.setInputValue('%s', '%s', {priority: 'event'})",
                                                          ns("approve_id"), id_) # Pass ID on click
              )), " ", # Space between buttons
              # Reject Button
              as.character(actionButton(inputId = ns(paste0("reject_", id_)),
                                        label = "Rejeter",
                                        icon = icon("times"),
                                        class = "btn-danger btn-sm",
                                        onclick = sprintf("Shiny.setInputValue('%s', '%s', {priority: 'event'})",
                                                          ns("reject_id"), id_) # Pass ID on click
              ))
            )
          })
          data$Actions <- actions
        } else {
          # Create empty tibble with Actions column if no data
          data <- tibble::add_column(data, Actions = character())
        }
        
        
        message("Validation Module: Found ", nrow(data), " pending submissions.")
        return(data)
        
      }, error = function(e) {
        showNotification(paste("Erreur lecture DB Décisionnelle:", e$message), type="error")
        message("Validation Module Error: ", e$message)
        return(tibble(Error = e$message, Actions = character())) # Return empty tibble on error
      })
      # Note: Mongo connection managed by mongolite's scoping
      
    }, ignoreNULL = FALSE) # Run once on load
    
    # --- Render DataTable ---
    output$pending_submissions_table <- DT::renderDataTable({
      df <- pending_data()
      req(df)
      
      # Need escape = FALSE to render HTML buttons
      # Careful with other columns if they could contain HTML/JS
      # Target only the 'Actions' column for non-escaping
      action_col_index <- which(colnames(df) == "Actions") - 1 # 0-based index for targets
      
      DT::datatable(
        df,
        rownames = FALSE,
        escape = -(action_col_index+1), # Escape all columns EXCEPT the Actions column (use negative index)
        # Alternative: escape=TRUE, and target actions with render=JS('function(data, type, row){ return data; }')? More complex.
        options = list(
          scrollX = TRUE,
          # order = list(list(1, 'desc')), # Default sort by timestamp maybe? column index 1 = timestamp
          paging = TRUE,
          searching = TRUE,
          # Tell DT that the Actions column contains HTML, shouldn't be searched/sorted normally
          columnDefs = list(list(targets = action_col_index, orderable = FALSE, searchable = FALSE))
        ),
        selection = 'none' # Disable row selection if using buttons in rows
      ) %>%
        # Optional: Format timestamp column nicely if needed
        formatDate(columns = "timestamp", method = "toLocaleString")
      
    })
    
    # --- Observe Button Clicks ---
    
    # Observe Approve Clicks
    observeEvent(input$approve_id, {
      submission_id <- input$approve_id
      message("Approving submission: ", submission_id)
      con <- NULL
      tryCatch({
        con <- get_decis_mongo_connection()
        req(con)
        # Update status to 'Approved' for the specific _id
        # MongoDB requires ObjectId for matching _id unless it was stored as string
        # Using $oid syntax in query if submission_id is character hex string
        update_query <- sprintf('{"_id": {"$oid": "%s"}}', submission_id)
        update_data <- '{"$set": {"status": "Approved"}}'
        
        result <- con$update(query = update_query, update = update_data, multiple = FALSE) # Update only one
        
        if(result$modifiedCount == 1) {
          showNotification(paste("Analyse", submission_id, "approuvée."), type="message")
          trigger_refresh(trigger_refresh() + 1) # Increment reactive value to refresh table
        } else {
          showNotification(paste("Échec approbation analyse", submission_id, "(document non trouvé ou déjà à jour?)."), type="warning")
        }
      }, error = function(e) {
        showNotification(paste("Erreur lors de l'approbation:", e$message), type="error")
        message("Validation Module Error (Approve): ", e$message)
      })
    })
    
    # Observe Reject Clicks
    observeEvent(input$reject_id, {
      submission_id <- input$reject_id
      message("Rejecting submission: ", submission_id)
      con <- NULL
      tryCatch({
        con <- get_decis_mongo_connection()
        req(con)
        # Update status to 'Rejected'
        update_query <- sprintf('{"_id": {"$oid": "%s"}}', submission_id)
        update_data <- '{"$set": {"status": "Rejected"}}'
        
        result <- con$update(query = update_query, update = update_data, multiple = FALSE)
        
        if(result$modifiedCount == 1) {
          showNotification(paste("Analyse", submission_id, "rejetée."), type="warning")
          trigger_refresh(trigger_refresh() + 1) # Refresh table
        } else {
          showNotification(paste("Échec rejet analyse", submission_id, "(non trouvé ou déjà à jour?)."), type="warning")
        }
      }, error = function(e) {
        showNotification(paste("Erreur lors du rejet:", e$message), type="error")
        message("Validation Module Error (Reject): ", e$message)
      })
    })
    
  }) # End moduleServer
}