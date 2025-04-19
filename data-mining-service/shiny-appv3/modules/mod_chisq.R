# modules/mod_chisq.R

# --- Libraries ---
library(shiny)
library(dplyr)
library(DT)
library(shinycssloaders)
library(writexl)
library(jsonlite)
library(mongolite)
library(tibble)
library(graphics) # For mosaicplot

# Note: Depends on global.R for DB connection helpers

# --- UI Function ---
mod_chisq_ui <- function(id) {
  ns <- NS(id)
  tagList(
    h3("Chi-Square Independence Test"),
    fluidRow(
      column(width = 3,
             wellPanel(
               h4("Chi² Options"),
               helpText("Tests if there is a statistically significant association between two categorical variables."),
               selectInput(ns("var1_select_chisq"), "Select the First Categorical Variable:",
                           choices = NULL),
               selectInput(ns("var2_select_chisq"), "Select the Second Categorical Variable:",
                           choices = NULL),
               hr(),
               actionButton(ns("run_chisq"), "Run/Update Chi² Test")
             ),
             wellPanel(
               h4("Actions"),
               textAreaInput(ns("conclusion_chisq"), "Conclusion / Interpretation:", rows = 4),
               actionButton(ns("save_chisq"), "Submit Chi² Analysis", icon = icon("database")),
               hr(),
               h5("Download Results:"),
               downloadButton(ns("dl_chisq_summary"), "Test Summary (.txt)"),
               downloadButton(ns("dl_chisq_table"), "Contingency Table (.xlsx)")
             )
      ), # End Options Column
      column(width = 9,
             h3("Chi-Square Results"),
             tabsetPanel(
               tabPanel("Chi-Square Test",
                        helpText("Tests the null hypothesis of independence. A low P-value (p.value) (< 0.05) suggests a significant association."),
                        withSpinner(verbatimTextOutput(ns("chisq_test_output")))
               ),
               tabPanel("Contingency Table",
                        helpText("Cross-tabulation showing observed counts for each combination."),
                        withSpinner(tableOutput(ns("contingency_table")))
               ),
               tabPanel("Expected Counts",
                        helpText("Expected counts if the variables were independent. Useful for verifying test conditions."),
                        withSpinner(tableOutput(ns("expected_table")))
               ),
               tabPanel("Mosaic Plot",
                        helpText("Visualization of the association."),
                        withSpinner(plotOutput(ns("mosaic_plot")))
               )
             ) # End tabsetPanel
      ) # End Results Column
    ) # End fluidRow
  ) # End tagList
}

# --- Server Function ---
mod_chisq_server <- function(id, data_reactive) { # Takes FULL enriched data
  moduleServer(id, function(input, output, session) {
    ns <- session$ns
    
    # --- Dynamic UI Updates ---
    categorical_vars <- reactive({
      df <- data_reactive(); req(df)
      names(df)[sapply(df, function(col) (is.factor(col) || is.character(col)) && n_distinct(col[!is.na(col)]) < 100 )]
    })
    observe({ updateSelectInput(session, "var1_select_chisq", choices = categorical_vars()) })
    observe({ req(input$var1_select_chisq); choices_var2 <- setdiff(categorical_vars(), input$var1_select_chisq); updateSelectInput(session, "var2_select_chisq", choices = choices_var2) })
    
    # --- Data Preparation & Test Execution ---
    chisq_results_list <- eventReactive(input$run_chisq, {
      df_full <- data_reactive(); var1 <- input$var1_select_chisq; var2 <- input$var2_select_chisq
      message("CHISQ EVENT: Triggered."); req(df_full, var1, var2, var1 %in% names(df_full), var2 %in% names(df_full), var1 != var2); message("CHISQ EVENT: Inputs Valid.")
      df_subset <- df_full %>% select( V1 = all_of(var1), V2 = all_of(var2) ) %>% mutate( V1 = as.factor(V1), V2 = as.factor(V2) ) %>% na.omit(); message("CHISQ EVENT: Subset Created (Rows: ", nrow(df_subset), ")")
      req(nrow(df_subset) > 1); message("CHISQ EVENT: Row Count Check Passed.")
      nlevel_v1 <- nlevels(droplevels(df_subset$V1)); nlevel_v2 <- nlevels(droplevels(df_subset$V2)); message("CHISQ EVENT: Levels V1=", nlevel_v1, ", Levels V2=", nlevel_v2); req(nlevel_v1 >= 2 , nlevel_v2 >= 2); message("CHISQ EVENT: Level Count Check Passed.")
      contingency_tbl <- table(df_subset$V1, df_subset$V2); message("CHISQ EVENT: Cont Table Created (Dims: ", paste(dim(contingency_tbl), collapse="x"), ")"); req(all(dim(contingency_tbl) >= 2)); message("CHISQ EVENT: Table Dim Check Passed.")
      
      showNotification("Calcul Test Chi²...", duration=2)
      chisq_res <- NULL; warning_msg <- NULL
      # Store warning if generated, let test proceed
      test_env <- new.env() # Environment to store result via <<-
      test_env$chisq_res <- NULL
      test_env$warning_msg <- NULL
      tryCatch(
        withCallingHandlers({
          test_env$chisq_res <- chisq.test(contingency_tbl)
          message("CHISQ DEBUG: chisq.test call finished.") # DEBUG Success inside tryCatch
        },
        warning = function(w) {
          message("CHISQ DEBUG: Warning captured: ", conditionMessage(w)) # DEBUG Capture Warning
          test_env$warning_msg <<- conditionMessage(w) # Store warning
          invokeRestart("muffleWarning") }
        ),
        error = function(e) {
          showNotification(paste("Erreur Chi²:", e$message), type = "error")
          message("CHISQ DEBUG: Error captured: ", e$message) # DEBUG Capture Error
          # chisq_res remains NULL
        }
      )
      # Copy result from environment
      chisq_res <- test_env$chisq_res
      warning_msg <- test_env$warning_msg
      
      # *** Explicit Check of result BEFORE final req ***
      if (is.null(chisq_res)) {
        message("CHISQ EVENT: chisq_res object is NULL after tryCatch.") # DEBUG NULL result
        showNotification("Calcul Chi² échoué ou résultat invalide.", type="warning")
        req(FALSE, .label="Chi-Squared Test Calculation Failed") # Make reactive invalid
      }
      message("CHISQ EVENT: Chi-sq test result object seems valid.")
      
      # Don't req expected, calculate it if possible
      expected_counts <- tryCatch(chisq_res$expected, error = function(e) { message("CHISQ DEBUG: Could not get expected counts."); NULL})
      # message("CHISQ DEBUG: Expected counts obtained (or NULL).")
      
      message("CHISQ EVENT: Returning results list.")
      return(list( contingency_table = contingency_tbl, chisq_result = chisq_res, expected_table = expected_counts, warning_message = warning_msg, var1 = var1, var2 = var2 ))
    }, ignoreNULL = TRUE)
    
    # --- Render Outputs ---
    output$chisq_test_output <- renderPrint({ results <- chisq_results_list(); req(results, results$chisq_result); if(!is.null(results$warning_message)) { cat("Avertissement du test Chi²:\n", results$warning_message, "\n\n") }; print(results$chisq_result) })
    output$contingency_table <- renderTable({ results <- chisq_results_list(); req(results, results$contingency_table); addmargins(results$contingency_table) }, rownames = TRUE, digits = 0)
    output$expected_table <- renderTable({ results <- chisq_results_list(); req(results, results$expected_table); round(results$expected_table, 2) }, rownames = TRUE)
    output$mosaic_plot <- renderPlot({ results <- chisq_results_list(); req(results, results$contingency_table); tryCatch({ mosaicplot(results$contingency_table, shade = TRUE, las=1, main = paste("Mosaic Plot:", results$var1, "vs", results$var2), xlab = results$var1, ylab = results$var2) }, error=function(e){ plot.new(); title(main="Erreur Plot Mosaic"); text(0.5,0.5, e$message)}) }, res=96)
    
    # --- Download Handlers ---
    output$dl_chisq_summary <- downloadHandler( filename = function() { req(chisq_results_list()); paste0('chisq_summary_', chisq_results_list()$var1, '_vs_', chisq_results_list()$var2, '_', Sys.Date(), '.txt') }, content = function(file) { req(chisq_results_list()); sink(file); cat("Chi-Squared Test Summary...\n"); cat("Var 1:", chisq_results_list()$var1, "\nVar 2:", chisq_results_list()$var2, "\n\nObserved:\n"); print(addmargins(chisq_results_list()$contingency_table)); cat("\nExpected:\n"); print(round(chisq_results_list()$expected_table, 2)); cat("\nTest Results:\n"); if(!is.null(chisq_results_list()$warning_message)){ cat("Warning:", chisq_results_list()$warning_message, "\n\n") }; print(chisq_results_list()$chisq_result); sink() } )
    output$dl_chisq_table <- downloadHandler( filename = function() { req(chisq_results_list()); paste0('chisq_table_', chisq_results_list()$var1, '_vs_', chisq_results_list()$var2, '_', Sys.Date(), '.xlsx') }, content = function(file) { req(chisq_results_list()); observed <- as.data.frame.matrix(addmargins(chisq_results_list()$contingency_table)) %>% tibble::rownames_to_column("Var1"); expected <- as.data.frame.matrix(round(chisq_results_list()$expected_table, 2)) %>% tibble::rownames_to_column("Var1"); writexl::write_xlsx( list(Observed_Counts = observed, Expected_Counts = expected), file ) } )
    
    # --- Save to DB Logic ---
    observeEvent(input$save_chisq, { req(chisq_results_list(), input$conclusion_chisq, nchar(trimws(input$conclusion_chisq)) >= 5); results <- chisq_results_list(); conclusion_text <- trimws(input$conclusion_chisq); chisq_res <- results$chisq_result; key_results_list <- list( variable_1 = results$var1, variable_2 = results$var2, x_squared = tryCatch(chisq_res$statistic, error=function(e) NA), df = tryCatch(chisq_res$parameter, error=function(e) NA), p_value = tryCatch(chisq_res$p.value, error=function(e) NA) ); data_to_save_list <- list( timestamp = Sys.time(), analysis_type = "Chi-Squared", selected_vars = paste(results$var1, "vs", results$var2), selected_axes = NA_character_, key_results = toJSON(key_results_list, auto_unbox=TRUE, pretty = FALSE), conclusion = conclusion_text, status = 'Pending Approval', analyst_id = Sys.getenv("USER", "unknown") ); con_decis_mongo <- get_decis_mongo_connection(); if (is.null(con_decis_mongo)) { showNotification("Erreur DB Décisionnelle.", type = "error"); return() }; tryCatch({ insert_result <- con_decis_mongo$insert(data_to_save_list); showNotification("Analyse Chi² soumise!", type = "message", duration = 5); updateTextAreaInput(session, ns("conclusion_chisq"), value = "") }, error = function(e) { message("MongoDB Save Error (Chi-Sq): ", e$message); showNotification(paste("Erreur sauvegarde DB:", e$message), type = "error", duration = 10) }) })
    
  }) # End moduleServer
}