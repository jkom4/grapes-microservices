    # modules/mod_acm.R

# --- Libraries needed by this module ---
library(shiny)
library(FactoMineR)
library(factoextra)
library(dplyr)
library(ggplot2)
library(DT)
library(shinycssloaders)
library(writexl)
library(jsonlite)
library(DBI)
# library(RMySQL) # Switched to Mongo for saving
# library(RMariaDB) # Switched to Mongo for saving
library(mongolite) # Make sure this is loaded
library(tibble)

# Note: get_decis_mongo_connection and safe_db_disconnect are expected from global.R
# safe_db_disconnect isn't used if saving only to Mongo

# --- UI Function ---
# (UI function mod_acm_ui remains the same as the previous version)
# --- UI Function ---
mod_acm_ui <- function(id) {
  ns <- NS(id)
  tagList(
    h3("Multiple Correspondence Analysis (MCA)"),
    fluidRow(
      column(width = 3,
             wellPanel(
               h4("MCA Options"),
               selectInput(ns("vars_select_mca"), "Select variables (factors) for MCA:",
                           choices = NULL, multiple = TRUE, selected = NULL),
               numericInput(ns("axe_x_mca"), "Horizontal Axis:", value = 1, min = 1, step = 1),
               numericInput(ns("axe_y_mca"), "Vertical Axis:", value = 2, min = 1, step = 1),
               actionButton(ns("run_mca"), "Run/Update MCA")
             ),
             wellPanel(
               h4("Input Data Preview (MCA)"),
               DT::dataTableOutput(ns("head_data_mca"))
             ),
             wellPanel(
               h4("Actions"),
               textAreaInput(ns("conclusion_mca"), "Conclusion / Interpretation:", rows = 4),
               actionButton(ns("save_mca"), "Submit Analysis", icon = icon("database")),
               hr(),
               h5("Download Results:"),
               downloadButton(ns("dl_mca_eig_plot"), "Scree Plot (.png)"),
               downloadButton(ns("dl_mca_biplot"), "Biplot (.png)"),
               downloadButton(ns("dl_mca_contrib_table"), "Contributions Table (.xlsx)"),
               downloadButton(ns("dl_mca_cos2_table"), "Cos2 Table (.xlsx)")
             )
      ),
      column(width = 9,
             h3("MCA Results"),
             tabsetPanel(
               tabPanel("Scree Plot of Eigenvalues", withSpinner(plotOutput(ns("mca_eig_plot")))),
               tabPanel("Biplot (Variables & Individuals)", withSpinner(plotOutput(ns("mca_biplot")))),
               tabPanel("Contributions (Variable Categories)",
                        fluidRow(
                          column(6, h4("Contribution to Axes (Table)"), withSpinner(DT::dataTableOutput(ns("mca_var_contrib_table")))),
                          column(6, h4("Contribution to Axis X"), withSpinner(plotOutput(ns("mca_var_contrib_plot_x"))))
                        ),
                        fluidRow(
                          column(6, offset=6, h4("Contribution to Axis Y"), withSpinner(plotOutput(ns("mca_var_contrib_plot_y"))))
                        )
               ),
               tabPanel("Cos2 Quality (Variable Categories)",
                        h4("Representation Quality (Cos2)"),
                        withSpinner(DT::dataTableOutput(ns("mca_var_cos2_table")))
               )
             ) # End tabsetPanel
      ) # End column
    ) # End fluidRow
  ) # End tagList
}

# --- Server Function ---

# modules/mod_acm.R (Server)

mod_acm_server <- function(id, data_reactive) {
  moduleServer(id, function(input, output, session) {
    ns <- session$ns
    
    # --- State Management ---
    # This reactiveVal is not strictly needed if using eventReactives for both steps
    # data_for_mca <- reactiveVal(NULL) # Can likely remove
    
    # --- Data Preparation Reactive (Triggered by Button) ---
    data_for_mca <- eventReactive(input$run_mca, {
      df_input <- data_reactive()
      # Req basic inputs first inside the eventReactive
      req(df_input, input$vars_select_mca, .label = "ACM Data Prep Input")
      
      selected_vars_exist <- input$vars_select_mca[input$vars_select_mca %in% names(df_input)]
      
      # Use req() for validation of selected variables
      req(length(selected_vars_exist) > 0, # Need at least one valid selection
          length(selected_vars_exist) >= 2, # Need at least two valid selections
          .label = "ACM Variable Selection")
      
      df_selected <- df_input %>% select(all_of(selected_vars_exist))
      
      # Use req() for validation of resulting data
      req(nrow(df_selected) > 1, .label = "ACM Selected Data Rows") # Need more than 1 row
      
      # If all req passes, show message and return
      showNotification("Données prêtes.", type="message", duration=2)
      return(df_selected)
    }, ignoreNULL = TRUE) # Requires button press
    
    # --- MCA Results Reactive (Triggered by Button) ---
    mca_results <- eventReactive(input$run_mca, {
      df_run <- data_for_mca() # Depends on the reactive above
      req(df_run)
      showNotification("Calcul ACM...", type="message", duration=2)
      res_mca <- tryCatch({ FactoMineR::MCA(df_run, graph = FALSE, ncp = 5) },
                          error = function(e){ showNotification(paste("Erreur ACM:", e$message), type="error", duration=10); return(NULL) })
      req(res_mca)
      return(res_mca)
    }, ignoreNULL = TRUE)
    
    # --- <<< MOVED THIS OBSERVER OUTSIDE eventReactive definitions >>> ---
    # Update variable selector when input data is ready
    observeEvent(data_reactive(), {
      message("ACM ObserveEvent: Triggered.") # DEBUG 1
      df_quali <- data_reactive() # Get the value
      
      # Add validation for content though
      if(is.null(df_quali)) {
        message("ACM ObserveEvent: df_quali is NULL.") # DEBUG NULL
        # showNotification("ACM: Received NULL data.", type="warning") # Optional UI feedback
        return() # Stop if NULL
      }
      if(!is.data.frame(df_quali)) {
        message("ACM ObserveEvent: df_quali is not a dataframe. Class: ", class(df_quali)) # DEBUG type
        showNotification("ACM: Received non-dataframe data.", type="error")
        return() # Stop if not df
      }
      if(ncol(df_quali) == 0) {
        message("ACM ObserveEvent: df_quali has 0 columns.") # DEBUG empty
        showNotification("ACM: Received dataframe with 0 columns.", type="warning")
        return() # Stop if empty
      }
      
      # If checks pass, proceed
      factor_cols <- names(df_quali) # Should all be factors already
      message("ACM ObserveEvent: Found columns: ", paste(factor_cols, collapse=", ")) # DEBUG Columns
      message("ACM ObserveEvent: Attempting updateSelectInput...") # DEBUG Update
      updateSelectInput(
        session = session,
        inputId = "vars_select_mca", # Ensure correct ID (no ns() needed here)
        choices = factor_cols,
        selected = factor_cols
      )
      message("ACM ObserveEvent: updateSelectInput finished.") # DEBUG Finished
    }, ignoreNULL = TRUE, ignoreInit = FALSE)
    # --- <<< END MOVE >>> ---
    
    
    # --- Data Preview ---
    output$head_data_mca <- DT::renderDataTable({
      req(data_for_mca()) # This depends on the button click
      DT::datatable( head(data_for_mca(), 10), rownames = FALSE, options = list( scrollX = TRUE, scrollY = "200px", dom = 't', paging = FALSE, searching = FALSE ))
    })
    
    # --- Selected Axes ---
    selected_axes_mca <- reactive({
      mca_res <- mca_results(); req(mca_res)
      # ... (rest of selected_axes_mca logic) ...
      max_dim <- ncol(mca_res$eig); req(input$axe_x_mca, input$axe_y_mca); req(input$axe_x_mca > 0, input$axe_y_mca > 0, input$axe_x_mca <= max_dim, input$axe_y_mca <= max_dim, input$axe_x_mca != input$axe_y_mca); c(input$axe_x_mca, input$axe_y_mca)
    })
    
    # --- Plot Objects ---
    # ... (Plot object definitions - should be ok now) ...
    eig_plot_mca_obj <- reactive({ req(mca_results()); fviz_eig(mca_results(), addlabels = TRUE, ggtheme = theme_minimal()) + ggtitle("Éboulis des valeurs propres (ACM)") })
    biplot_mca_obj <- reactive({ req(mca_results(), selected_axes_mca()); axes_choice <- selected_axes_mca(); fviz_mca_biplot(mca_results(), axes = axes_choice, repel = TRUE, col.var = "contrib", gradient.cols = c("#00AFBB", "#E7B800", "#FC4E07"), ggtheme = theme_minimal()) + ggtitle(paste("Biplot ACM (Axes", axes_choice[1], "&", axes_choice[2], ")")) })
    get_mca_var_results <- reactive({ req(mca_results()); get_mca_var(mca_results()) })
    contrib_plot_mca_x_obj <- reactive({ mca_var <- get_mca_var_results(); req(mca_var); axes_choice <- selected_axes_mca(); req(axes_choice); contrib_data_all <- as.data.frame(mca_var$contrib) %>% tibble::rownames_to_column("Categorie"); dim_x_name <- paste("Dim", axes_choice[1]); req(dim_x_name %in% names(contrib_data_all)); contrib_df <- contrib_data_all %>% select(Categorie, Dim = !!sym(dim_x_name)) %>% arrange(desc(Dim)) %>% slice_head(n = 20); req(nrow(contrib_df) > 0); ggplot(contrib_df, aes(x = reorder(Categorie, Dim), y = Dim)) + geom_col(fill = "#00AFBB") + coord_flip() + ggtitle(paste("Top 20 Contributions Cat. Axe", axes_choice[1])) + theme_minimal() + labs(x="Catégorie", y="Contribution (%)") })
    contrib_plot_mca_y_obj <- reactive({ mca_var <- get_mca_var_results(); req(mca_var); axes_choice <- selected_axes_mca(); req(axes_choice); contrib_data_all <- as.data.frame(mca_var$contrib) %>% tibble::rownames_to_column("Categorie"); dim_y_name <- paste("Dim", axes_choice[2]); req(dim_y_name %in% names(contrib_data_all)); contrib_df <- contrib_data_all %>% select(Categorie, Dim = !!sym(dim_y_name)) %>% arrange(desc(Dim)) %>% slice_head(n = 20); req(nrow(contrib_df) > 0); ggplot(contrib_df, aes(x = reorder(Categorie, Dim), y = Dim)) + geom_col(fill = "#E7B800") + coord_flip() + ggtitle(paste("Top 20 Contributions Cat. Axe", axes_choice[2])) + theme_minimal() + labs(x="Catégorie", y="Contribution (%)") })
    
    
    # --- Render Plots ---
    output$mca_eig_plot <- renderPlot({ req(eig_plot_mca_obj()); eig_plot_mca_obj() })
    output$mca_biplot <- renderPlot({ req(biplot_mca_obj()); biplot_mca_obj() })
    output$mca_var_contrib_plot_x <- renderPlot({ req(contrib_plot_mca_x_obj()); contrib_plot_mca_x_obj() })
    output$mca_var_contrib_plot_y <- renderPlot({ req(contrib_plot_mca_y_obj()); contrib_plot_mca_y_obj() })
    
    # --- Table Data Reactives ---
    contrib_data_mca_reactive <- reactive({ req(get_mca_var_results()); as.data.frame(get_mca_var_results()$contrib) })
    cos2_data_mca_reactive <- reactive({ req(get_mca_var_results()); as.data.frame(get_mca_var_results()$cos2) })
    
    # --- Render Tables ---
    output$mca_var_contrib_table <- DT::renderDataTable({ req(contrib_data_mca_reactive()); DT::datatable(round(contrib_data_mca_reactive(), 3), rownames = TRUE, options = list(scrollX = TRUE, scrollY = "300px", paging=FALSE, searching=TRUE)) })
    output$mca_var_cos2_table <- DT::renderDataTable({ req(cos2_data_mca_reactive()); DT::datatable(round(cos2_data_mca_reactive(), 3), rownames = TRUE, options = list(scrollX = TRUE, scrollY = "300px", paging=FALSE, searching=TRUE)) })
    
    # --- Download Handlers ---
    # ... (download handlers remain the same) ...
    output$dl_mca_eig_plot <- downloadHandler( filename = function() { paste0('mca_eigenvalue_', Sys.Date(), '.png') }, content = function(file) { req(eig_plot_mca_obj()); ggsave(file, plot = eig_plot_mca_obj(), device = "png", width = 8, height = 6) } )
    output$dl_mca_biplot <- downloadHandler( filename = function() { paste0('mca_biplot_', Sys.Date(), '.png') }, content = function(file) { req(biplot_mca_obj()); ggsave(file, plot = biplot_mca_obj(), device = "png", width = 8, height = 8) } )
    output$dl_mca_contrib_table <- downloadHandler( filename = function() { paste0('mca_contributions_', Sys.Date(), '.xlsx') }, content = function(file) { req(contrib_data_mca_reactive()); writexl::write_xlsx( list(Contributions = contrib_data_mca_reactive() %>% tibble::rownames_to_column("Categorie")), file ) } )
    output$dl_mca_cos2_table <- downloadHandler( filename = function() { paste0('mca_cos2_', Sys.Date(), '.xlsx') }, content = function(file) { req(cos2_data_mca_reactive()); writexl::write_xlsx( list(Cos2 = cos2_data_mca_reactive() %>% tibble::rownames_to_column("Categorie")), file ) } )
    
    
    # --- Save to DB Logic ---
    # (Save logic remains the same)
    observeEvent(input$save_mca, { req(mca_results(), input$vars_select_mca, input$conclusion_mca); conclusion_text <- trimws(input$conclusion_mca); if(nchar(conclusion_text) < 5){ showNotification("La conclusion doit contenir au moins 5 caractères.", type = "warning", duration=5); return() }; showNotification("Soumission en cours...", type="message", duration = NULL, id="saving_mca"); on.exit(removeNotification("saving_mca"), add = TRUE); res.mca <- mca_results(); selected_vars_str <- paste(input$vars_select_mca, collapse = ", "); current_axes <- tryCatch(selected_axes_mca(), error=function(e) NULL); req(current_axes); axes_str <- paste(current_axes, collapse = ","); eig_summary <- tryCatch(toJSON(as.data.frame(res.mca$eig), auto_unbox = TRUE, pretty = FALSE), error = function(e) {NA_character_}); data_to_save_list <- list( timestamp = Sys.time(), analysis_type = "ACM", selected_vars = I(input$vars_select_mca), selected_axes = current_axes, key_results_eigenvalues = as.data.frame(res.mca$eig), conclusion = conclusion_text, status = 'Pending Approval', analyst_id = Sys.getenv("USER", "unknown") ); con_decis_mongo <- get_decis_mongo_connection(); if (is.null(con_decis_mongo)) { showNotification("Erreur Connexion DB Décisionnelle (Mongo).", type = "error"); return() }; tryCatch({ insert_result <- con_decis_mongo$insert(data_to_save_list); showNotification("Analyse ACM soumise avec succès!", type = "message", duration = 5); updateTextAreaInput(session, ns("conclusion_mca"), value = "") }, error = function(e) { message("MongoDB Save Error: ", e$message); showNotification(paste("Erreur sauvegarde DB (Mongo):", e$message), type = "error", duration = 10) }) })
    
    
  }) # End moduleServer
}
