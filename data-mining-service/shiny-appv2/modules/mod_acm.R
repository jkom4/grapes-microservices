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
mod_acm_ui <- function(id) {
  ns <- NS(id)
  tagList(
    h3("Analyse des Correspondances Multiples (ACM)"),
    fluidRow(
      column(width = 3,
             wellPanel(
               h4("Options ACM"),
               selectInput(ns("vars_select_mca"), "Choisir les variables (facteurs) pour l'ACM:",
                           choices = NULL, multiple = TRUE, selected = NULL),
               numericInput(ns("axe_x_mca"), "Axe horizontal:", value = 1, min = 1, step = 1),
               numericInput(ns("axe_y_mca"), "Axe vertical:", value = 2, min = 1, step = 1),
               actionButton(ns("run_mca"), "Lancer/Actualiser ACM")
             ),
             wellPanel(
               h4("Aperçu Données d'Entrée (ACM)"),
               DT::dataTableOutput(ns("head_data_mca"))
             ),
             wellPanel(
               h4("Actions"),
               textAreaInput(ns("conclusion_mca"), "Conclusion / Interprétation:", rows = 4),
               actionButton(ns("save_mca"), "Soumettre l'analyse", icon = icon("database")),
               hr(),
               h5("Télécharger les Résultats:"),
               downloadButton(ns("dl_mca_eig_plot"), "Graphique Éboulis (.png)"),
               downloadButton(ns("dl_mca_biplot"), "Biplot (.png)"),
               downloadButton(ns("dl_mca_contrib_table"), "Table Contributions (.xlsx)"),
               downloadButton(ns("dl_mca_cos2_table"), "Table Cos2 (.xlsx)")
             )
      ),
      column(width = 9,
             h3("Résultats ACM"),
             tabsetPanel(
               tabPanel("Éboulis des valeurs propres", withSpinner(plotOutput(ns("mca_eig_plot")))),
               tabPanel("Biplot (Variables & Individus)", withSpinner(plotOutput(ns("mca_biplot")))),
               tabPanel("Contributions (Catégories Var.)",
                        fluidRow(
                          column(6, h4("Contribution aux Axes (Table)"), withSpinner(DT::dataTableOutput(ns("mca_var_contrib_table")))),
                          column(6, h4("Contribution à l'Axe X"), withSpinner(plotOutput(ns("mca_var_contrib_plot_x"))))
                        ),
                        fluidRow(
                          column(6, offset=6, h4("Contribution à l'Axe Y"), withSpinner(plotOutput(ns("mca_var_contrib_plot_y"))))
                        )
               ),
               tabPanel("Qualité Cos2 (Catégories Var.)",
                        h4("Qualité de Représentation (Cos2)"),
                        withSpinner(DT::dataTableOutput(ns("mca_var_cos2_table")))
               )
             ) # End tabsetPanel
      ) # End column
    ) # End fluidRow
  ) # End tagList
}

# --- Server Function ---
mod_acm_server <- function(id, data_reactive) {
  moduleServer(id, function(input, output, session) {
    ns <- session$ns
    
    data_for_mca <- reactiveVal(NULL) # reactiveVal holds data for MCA
    
    # Update variable selector
    observe({
      df_quali <- data_reactive()
      # Use req() to ensure df_quali is valid before proceeding
      req(df_quali, is.data.frame(df_quali), ncol(df_quali) > 0)
      factor_cols <- names(df_quali)
      updateSelectInput(session, "vars_select_mca", choices = factor_cols, selected = factor_cols)
    })
    
    # Filter data on button click
    observeEvent(input$run_mca, {
      df_input <- data_reactive()
      req(df_input, input$vars_select_mca) # Initial req is fine
      selected_vars_exist <- input$vars_select_mca[input$vars_select_mca %in% names(df_input)]
      
      # Use req() instead of validate() for these checks
      req(length(selected_vars_exist) > 0) # Stop if no valid vars selected
      
      df_selected <- df_input %>% select(all_of(selected_vars_exist))
      
      # Use req() for dimension checks
      req(ncol(df_selected) >= 2) # Stop if fewer than 2 columns
      req(nrow(df_selected) > 1) # Stop if 0 or 1 row
      
      # --- Alternative using if/showNotification (more user-friendly) ---
      # if(length(selected_vars_exist) == 0) {
      #     showNotification("Aucune variable valide sélectionnée.", type="warning")
      #     return() # Stop
      # }
      # df_selected <- df_input %>% select(all_of(selected_vars_exist))
      # if(ncol(df_selected) < 2) {
      #     showNotification("Veuillez sélectionner au moins 2 variables.", type="warning")
      #     return() # Stop
      # }
      # if(nrow(df_selected) <= 1) {
      #      showNotification("Pas assez de données (lignes) pour les variables sélectionnées.", type="warning")
      #      return() # Stop
      # }
      # -----------------------------------------------------------------
      
      # If all req() pass (or if conditions met), update the reactiveVal
      data_for_mca(df_selected)
      showNotification("Données prêtes pour l'analyse ACM.", type="message", duration=3) # Optional feedback
    })
    
    # Reactive for MCA results (depends on data_for_mca reactiveVal)
    mca_results <- reactive({
      # This reactive now depends on data_for_mca(), which is only set after 'run_mca' is clicked
      req(data_for_mca())
      df_run <- data_for_mca()
      showNotification("Calcul de l'ACM en cours...", type="message", duration=2)
      tryCatch({
        FactoMineR::MCA(df_run, graph = FALSE, ncp = 5)
      }, error = function(e){
        showNotification(paste("Erreur ACM:", e$message), type="error", duration=10)
        return(NULL)
      })
    })
    
    # Data Preview (depends on data_for_mca reactiveVal)
    output$head_data_mca <- DT::renderDataTable({
      req(data_for_mca()); # Ensure data is set after button click
      DT::datatable( head(data_for_mca(), 10), rownames = FALSE,
                     options = list( scrollX = TRUE, scrollY = "200px", dom = 't', paging = FALSE, searching = FALSE ))
    })
    
    # Selected Axes - Use req() for validation
    selected_axes_mca <- reactive({
      mca_res <- mca_results(); req(mca_res)
      max_dim <- ncol(mca_res$eig)
      # Use req() for basic input checks first
      req(input$axe_x_mca, input$axe_y_mca,
          input$axe_x_mca > 0, input$axe_y_mca > 0,
          input$axe_x_mca <= max_dim,
          input$axe_y_mca <= max_dim,
          input$axe_x_mca != input$axe_y_mca) # Check all conditions with req
      
      # If all req() pass, return the axes
      c(input$axe_x_mca, input$axe_y_mca)
    })
    
    # Helper reactive for category results (depends on mca_results)
    get_mca_var_results <- reactive({ req(mca_results()); get_mca_var(mca_results()) })
    
    # Plot Objects (depend on mca_results & selected_axes_mca)
    eig_plot_mca_obj <- reactive({ req(mca_results()); fviz_eig(mca_results(), addlabels = TRUE, ggtheme = theme_minimal()) + ggtitle("Éboulis des valeurs propres (ACM)") })
    biplot_mca_obj <- reactive({ req(mca_results(), selected_axes_mca()); axes_choice <- selected_axes_mca(); fviz_mca_biplot(mca_results(), axes = axes_choice, repel = TRUE, col.var = "contrib", gradient.cols = c("#00AFBB", "#E7B800", "#FC4E07"), ggtheme = theme_minimal()) + ggtitle(paste("Biplot ACM (Axes", axes_choice[1], "&", axes_choice[2], ")")) })
    contrib_plot_mca_x_obj <- reactive({ mca_var <- get_mca_var_results(); req(mca_var); axes_choice <- selected_axes_mca(); req(axes_choice); contrib_data_all <- as.data.frame(mca_var$contrib) %>% tibble::rownames_to_column("Categorie"); dim_x_name <- paste("Dim", axes_choice[1]); req(dim_x_name %in% names(contrib_data_all)); contrib_df <- contrib_data_all %>% select(Categorie, Dim = !!sym(dim_x_name)) %>% arrange(desc(Dim)) %>% slice_head(n = 20); req(nrow(contrib_df) > 0); ggplot(contrib_df, aes(x = reorder(Categorie, Dim), y = Dim)) + geom_col(fill = "#00AFBB") + coord_flip() + ggtitle(paste("Top 20 Contributions Cat. Axe", axes_choice[1])) + theme_minimal() + labs(x="Catégorie", y="Contribution (%)") })
    contrib_plot_mca_y_obj <- reactive({ mca_var <- get_mca_var_results(); req(mca_var); axes_choice <- selected_axes_mca(); req(axes_choice); contrib_data_all <- as.data.frame(mca_var$contrib) %>% tibble::rownames_to_column("Categorie"); dim_y_name <- paste("Dim", axes_choice[2]); req(dim_y_name %in% names(contrib_data_all)); contrib_df <- contrib_data_all %>% select(Categorie, Dim = !!sym(dim_y_name)) %>% arrange(desc(Dim)) %>% slice_head(n = 20); req(nrow(contrib_df) > 0); ggplot(contrib_df, aes(x = reorder(Categorie, Dim), y = Dim)) + geom_col(fill = "#E7B800") + coord_flip() + ggtitle(paste("Top 20 Contributions Cat. Axe", axes_choice[2])) + theme_minimal() + labs(x="Catégorie", y="Contribution (%)") })
    
    # Render Plots
    output$mca_eig_plot <- renderPlot({ req(eig_plot_mca_obj()); eig_plot_mca_obj() })
    output$mca_biplot <- renderPlot({ req(biplot_mca_obj()); biplot_mca_obj() })
    output$mca_var_contrib_plot_x <- renderPlot({ req(contrib_plot_mca_x_obj()); contrib_plot_mca_x_obj() })
    output$mca_var_contrib_plot_y <- renderPlot({ req(contrib_plot_mca_y_obj()); contrib_plot_mca_y_obj() })
    
    # Table Data Reactives
    contrib_data_mca_reactive <- reactive({ req(get_mca_var_results()); as.data.frame(get_mca_var_results()$contrib) })
    cos2_data_mca_reactive <- reactive({ req(get_mca_var_results()); as.data.frame(get_mca_var_results()$cos2) })
    
    # Render Tables
    output$mca_var_contrib_table <- DT::renderDataTable({ req(contrib_data_mca_reactive()); DT::datatable(round(contrib_data_mca_reactive(), 3), rownames = TRUE, options = list(scrollX = TRUE, scrollY = "300px", paging=FALSE, searching=TRUE)) })
    output$mca_var_cos2_table <- DT::renderDataTable({ req(cos2_data_mca_reactive()); DT::datatable(round(cos2_data_mca_reactive(), 3), rownames = TRUE, options = list(scrollX = TRUE, scrollY = "300px", paging=FALSE, searching=TRUE)) })
    
    # Download Handlers
    output$dl_mca_eig_plot <- downloadHandler( filename = function() { paste0('mca_eigenvalue_', Sys.Date(), '.png') }, content = function(file) { req(eig_plot_mca_obj()); ggsave(file, plot = eig_plot_mca_obj(), device = "png", width = 8, height = 6) } )
    output$dl_mca_biplot <- downloadHandler( filename = function() { paste0('mca_biplot_', Sys.Date(), '.png') }, content = function(file) { req(biplot_mca_obj()); ggsave(file, plot = biplot_mca_obj(), device = "png", width = 8, height = 8) } )
    output$dl_mca_contrib_table <- downloadHandler( filename = function() { paste0('mca_contributions_', Sys.Date(), '.xlsx') }, content = function(file) { req(contrib_data_mca_reactive()); writexl::write_xlsx( list(Contributions = contrib_data_mca_reactive() %>% tibble::rownames_to_column("Categorie")), file ) } )
    output$dl_mca_cos2_table <- downloadHandler( filename = function() { paste0('mca_cos2_', Sys.Date(), '.xlsx') }, content = function(file) { req(cos2_data_mca_reactive()); writexl::write_xlsx( list(Cos2 = cos2_data_mca_reactive() %>% tibble::rownames_to_column("Categorie")), file ) } )
    
    # modules/mod_acmreq()` just stops execution silently in that context.
    
  
    
    # --- Save to DB Logic (Using MongoDB) ---
    observeEvent(input$save_mca, {
      # Check prerequisites
      req(mca_results(), input$vars_select_mca, input$conclusion_mca)
      
      # Check conclusion length
      conclusion_text <- trimws(input$conclusion_mca)
      if(nchar(conclusion_text) < 5){
        showNotification("La conclusion doit contenir au moins 5 caractères.", type = "warning", duration=5)
        return()
      }
      
      # Show busy indicator
      showNotification("Soumission en cours...", type="message", duration = NULL, id="saving_mca")
      on.exit(removeNotification("saving_mca"), add = TRUE)
      
      # Prepare data
      res.mca <- mca_results(); selected_vars_str <- paste(input$vars_select_mca, collapse = ", "); current_axes <- tryCatch(selected_axes_mca(), error=function(e) NULL); req(current_axes); axes_str <- paste(current_axes, collapse = ",")
      eig_summary <- tryCatch(toJSON(as.data.frame(res.mca$eig), auto_unbox = TRUE, pretty = FALSE), error = function(e) {NA_character_})
      
      data_to_save_list <- list(
        timestamp = Sys.time(),
        analysis_type = "ACM",
        selected_vars = I(input$vars_select_mca),
        selected_axes = current_axes,
        key_results_eigenvalues = as.data.frame(res.mca$eig),
        conclusion = conclusion_text,
        status = 'Pending Approval',
        analyst_id = Sys.getenv("USER", "unknown")
      )
      
      # Get connection
      con_decis_mongo <- get_decis_mongo_connection()
      if (is.null(con_decis_mongo)) {
        # removeNotification handled by on.exit
        showNotification("Erreur Connexion DB Décisionnelle (Mongo).", type = "error"); return()
      }
      
      tryCatch({
        # Corrected insert call
        insert_result <- con_decis_mongo$insert(data_to_save_list)
        
        # removeNotification handled by on.exit
        # Success notification
        showNotification("Analyse ACM soumise avec succès!", type = "message", duration = 5)
        
        # Corrected update call (added ns())
        updateTextAreaInput(session, ns("conclusion_mca"), value = "")
        
      }, error = function(e) {
        # removeNotification handled by on.exit
        message("MongoDB Save Error: ", e$message)
        showNotification(paste("Erreur sauvegarde DB (Mongo):", e.message), type = "error", duration = 10)
      })
    }) # End observeEvent
  }) # End moduleServer
}
