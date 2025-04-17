# modules/mod_acp.R

# --- Libraries needed by this module ---
library(shiny)
library(FactoMineR)
library(factoextra)
library(dplyr)
library(ggplot2)
library(corrplot)
library(DT)
library(shinycssloaders)
library(writexl)
library(mongolite) # Ensure mongolite is loaded
library(jsonlite)

# --- UI Function ---
mod_acp_ui <- function(id) {
  ns <- NS(id)
  tagList(
    fluidRow(
      column(width = 3,
             wellPanel(
               h4("PCA Options"),
               helpText("Select non-redundant quantitative variables."),
               selectInput(ns("vars_select"), "Variables for PCA:",
                           choices = NULL, multiple = TRUE, selected = NULL),
               numericInput(ns("axe_x"), "Horizontal Axis:", value = 1, min = 1, step = 1),
               numericInput(ns("axe_y"), "Vertical Axis:", value = 2, min = 1, step = 1),
               actionButton(ns("run_pca"), "Run/Update PCA")
             ),
             wellPanel(
               h4("Input Data Preview"),
               DT::dataTableOutput(ns("head_data_pca"))
             ),
             wellPanel(
               h4("Actions"),
               textAreaInput(ns("conclusion_pca"), "Conclusion / Interpretation:", rows = 4),
               actionButton(ns("save_pca"), "Submit Analysis", icon = icon("database")),
               hr(),
               h5("Download Results:"),
               downloadButton(ns("dl_pca_eig_plot"), "Scree Plot (.png)"),
               downloadButton(ns("dl_pca_ind_plot"), "Individuals Plot (.png)"),
               downloadButton(ns("dl_pca_var_plot"), "Correlation Circle (.png)"),
               downloadButton(ns("dl_pca_contrib_table"), "Contributions Table (.xlsx)"),
               downloadButton(ns("dl_pca_cos2_table"), "Cos2 Table (.xlsx)")
             )
      ),
      column(width = 9,
             h3("PCA Results"),
             tabsetPanel(
               tabPanel("Scree Plot of Eigenvalues", withSpinner(plotOutput(ns("eig_plot")))),
               tabPanel("Individuals Plot", withSpinner(plotOutput(ns("pca_ind_plot")))),
               tabPanel("Correlation Circle", withSpinner(plotOutput(ns("pca_var_plot")))),
               tabPanel("Contributions (Variables)",
                        fluidRow(
                          column(6, h4("Contribution to Axes (Table)"), withSpinner(DT::dataTableOutput(ns("pca_var_contrib_table")))),
                          column(6, h4("Contribution to Axis X"), withSpinner(plotOutput(ns("pca_var_contrib_plot_x"))))
                        ),
                        fluidRow(
                          column(6, offset=6, h4("Contribution to Axis Y"), withSpinner(plotOutput(ns("pca_var_contrib_plot_y"))))
                        )
               ),
               tabPanel("Cos2 Quality (Variables)",
                        h4("Representation Quality (Cos2)"),
                        withSpinner(DT::dataTableOutput(ns("pca_var_cos2_table")))
               ),
               tabPanel("Correlation Matrix", withSpinner(plotOutput(ns("corr_plot")))
               )
             ) # End tabsetPanel
      ) # End column
    ) # End fluidRow
  ) # End tagList
}


# --- Server Function ---
mod_acp_server <- function(id, data_reactive) {
  moduleServer(id, function(input, output, session) {
    ns <- session$ns
    
    # Update variable selector
    observe({ df_quanti <- data_reactive(); req(df_quanti); numeric_cols <- names(df_quanti)[sapply(df_quanti, is.numeric)]; if(length(numeric_cols) == 0) { shiny::showNotification("Aucune colonne numérique trouvée.", type="warning"); updateSelectInput(session, "vars_select", choices = character(0), selected = character(0)) } else { updateSelectInput(session, "vars_select", choices = numeric_cols, selected = numeric_cols) } })
    
    # Reactive for selected data for PCA
    data_for_pca <- eventReactive(input$run_pca, {
      df <- data_reactive(); req(df, input$vars_select)
      selected_vars_exist <- input$vars_select[input$vars_select %in% names(df)]
      # Removed validate() call that was causing errors
      req(length(selected_vars_exist) > 0) # Use req instead for condition check
      df_selected <- df %>% select(all_of(selected_vars_exist))
      # Removed validate() call that was causing errors
      req(ncol(df_selected) >= 2, nrow(df_selected) > 0) # Use req instead
      return(df_selected)
    }, ignoreNULL = TRUE)
    
    # Reactive for PCA results
    acp_results <- reactive({ req(data_for_pca()); df_pca <- data_for_pca(); tryCatch({ FactoMineR::PCA(df_pca, scale.unit = TRUE, graph = FALSE, ncp = 5) }, error = function(e){ showNotification(paste("Erreur PCA:", e$message), type="error", duration=10); return(NULL) }) })
    
    # Data Preview Output
    output$head_data_pca <- DT::renderDataTable({ req(data_for_pca()); DT::datatable( head(data_for_pca(), 10), rownames = FALSE, options = list( scrollX = TRUE, scrollY = "200px", dom = 't', paging = FALSE, searching = FALSE )) })
    
    # modules/mod_acp.R (Server)
    
    # Selected Axes Reactive - Corrected Validation
    selected_axes <- reactive({
      pca_res <- acp_results(); req(pca_res)
      max_dim <- ncol(pca_res$eig)
      # Use req() for basic input checks first
      req(input$axe_x, input$axe_y,
          input$axe_x > 0, input$axe_y > 0, # Ensure positive
          input$axe_x <= max_dim,          # Ensure within bounds
          input$axe_y <= max_dim,
          input$axe_x != input$axe_y)      # Ensure different
      
      # If all req() pass, return the axes
      c(input$axe_x, input$axe_y)
      
      # --- Alternative using validate() correctly (if specific messages are desired) ---
      # req(input$axe_x, input$axe_y) # Still req basic input first
      # validate(
      #     need(input$axe_x > 0 && input$axe_y > 0, "Les axes doivent être positifs."),
      #     need(input$axe_x <= max_dim && input$axe_y <= max_dim, paste("Les axes doivent être <= ", max_dim)),
      #     need(input$axe_x != input$axe_y, "Les axes X et Y doivent être différents.")
      # )
      # # If validate passes, return axes
      # c(input$axe_x, input$axe_y)
      # ---------------------------------------------------------------------------------
      
    })
      
    
    # Plot Objects
    eig_plot_obj <- reactive({ req(acp_results()); fviz_eig(acp_results(), addlabels = TRUE, ylim = c(0, 60)) + ggtitle("Éboulis des valeurs propres") })
    ind_plot_obj <- reactive({ req(acp_results(), selected_axes()); fviz_pca_ind(acp_results(), axes = selected_axes(), repel = TRUE) + ggtitle(paste("Individus (Axes", selected_axes()[1], "&", selected_axes()[2], ")")) })
    var_plot_obj <- reactive({ req(acp_results(), selected_axes()); fviz_pca_var(acp_results(), axes = selected_axes(), repel = TRUE, col.var = "cos2") + scale_color_gradient2(low="grey", mid="blue", high="red", midpoint=0.5) + ggtitle(paste("Variables (Axes", selected_axes()[1], "&", selected_axes()[2], ")")) })
    contrib_plot_x_obj <- reactive({ req(acp_results(), selected_axes()); fviz_contrib(acp_results(), choice = "var", axes = selected_axes()[1], top = 15) + ggtitle(paste("Contribution Var. Axe", selected_axes()[1])) + theme(axis.text.x = element_text(angle = 45, hjust = 1)) })
    contrib_plot_y_obj <- reactive({ req(acp_results(), selected_axes()); fviz_contrib(acp_results(), choice = "var", axes = selected_axes()[2], top = 15) + ggtitle(paste("Contribution Var. Axe", selected_axes()[2])) + theme(axis.text.x = element_text(angle = 45, hjust = 1)) })
    
    # Render Plots
    output$eig_plot <- renderPlot({ req(eig_plot_obj()); eig_plot_obj() })
    output$pca_ind_plot <- renderPlot({ req(ind_plot_obj()); ind_plot_obj() })
    output$pca_var_plot <- renderPlot({ req(var_plot_obj()); var_plot_obj() })
    output$pca_var_contrib_plot_x <- renderPlot({ req(contrib_plot_x_obj()); contrib_plot_x_obj() })
    output$pca_var_contrib_plot_y <- renderPlot({ req(contrib_plot_y_obj()); contrib_plot_y_obj() })
    output$corr_plot <- renderPlot({ req(data_for_pca()); df_corr <- data_for_pca(); corr_matrix <- cor(df_corr, use = "pairwise.complete.obs"); corrplot::corrplot(corr_matrix, method = "color", type = "lower", order = "hclust", addCoef.col = "black", tl.col = "black", tl.srt = 45, diag = FALSE, title = "Matrice de Corrélation", mar = c(1,1,2,1)) })
    
    # Table Data Reactives
    contrib_data_reactive <- reactive({ req(acp_results()); as.data.frame(acp_results()$var$contrib) })
    cos2_data_reactive <- reactive({ req(acp_results()); as.data.frame(acp_results()$var$cos2) })
    
    # Render Tables
    output$pca_var_contrib_table <- DT::renderDataTable({ req(contrib_data_reactive()); DT::datatable(round(contrib_data_reactive(), 3), rownames = TRUE, options = list(scrollX = TRUE, scrollY = "300px", paging=FALSE, searching=TRUE)) })
    output$pca_var_cos2_table <- DT::renderDataTable({ req(cos2_data_reactive()); DT::datatable(round(cos2_data_reactive(), 3), rownames = TRUE, options = list(scrollX = TRUE, scrollY = "300px", paging=FALSE, searching=TRUE)) })
    
    # Download Handlers
    output$dl_pca_eig_plot <- downloadHandler( filename = function() { paste0('pca_eigenvalue_', Sys.Date(), '.png') }, content = function(file) { req(eig_plot_obj()); ggsave(file, plot = eig_plot_obj(), device = "png", width = 8, height = 6) } )
    output$dl_pca_ind_plot <- downloadHandler( filename = function() { paste0('pca_individuals_', Sys.Date(), '.png') }, content = function(file) { req(ind_plot_obj()); ggsave(file, plot = ind_plot_obj(), device = "png", width = 7, height = 7) } )
    output$dl_pca_var_plot <- downloadHandler( filename = function() { paste0('pca_variables_', Sys.Date(), '.png') }, content = function(file) { req(var_plot_obj()); ggsave(file, plot = var_plot_obj(), device = "png", width = 7, height = 7) } )
    output$dl_pca_contrib_table <- downloadHandler( filename = function() { paste0('pca_contributions_', Sys.Date(), '.xlsx') }, content = function(file) { req(contrib_data_reactive()); writexl::write_xlsx( list(Contributions = contrib_data_reactive() %>% tibble::rownames_to_column("Variable")), file ) } )
    output$dl_pca_cos2_table <- downloadHandler( filename = function() { paste0('pca_cos2_', Sys.Date(), '.xlsx') }, content = function(file) { req(cos2_data_reactive()); writexl::write_xlsx( list(Cos2 = cos2_data_reactive() %>% tibble::rownames_to_column("Variable")), file ) } )
    
    
    # --- Save to DB Logic (Using MongoDB) ---
    # modules/mod_acp.R (Server)
    
    # --- Save to DB Logic ---
    # --- Save to DB Logic ---
    observeEvent(input$save_pca, {
      # Check prerequisites that *must* exist first with req()
      req(acp_results(), input$vars_select, input$conclusion_pca)
      
      # Now check the conclusion length separately with an 'if' condition
      conclusion_text <- trimws(input$conclusion_pca)
      if(nchar(conclusion_text) < 5){
        showNotification("La conclusion doit contenir au moins 5 caractères.", type = "warning", duration=5)
        return() # Stop processing if too short
      }
      
      # If we get here, all checks passed - show busy indicator
      showNotification("Soumission en cours...", type="message", duration = NULL, id="saving_pca") # Keep it until done
      # Ensure removal on exit, even if errors occur later
      on.exit(removeNotification("saving_pca"), add = TRUE)
      
      # Proceed with preparing data (all reqs passed)
      res.pca <- acp_results(); selected_vars_str <- paste(input$vars_select, collapse = ", "); current_axes <- tryCatch(selected_axes(), error=function(e) NULL); req(current_axes); axes_str <- paste(current_axes, collapse = ",")
      eig_summary <- tryCatch(toJSON(as.data.frame(res.pca$eig), auto_unbox = TRUE, pretty=FALSE), error = function(e) {NA_character_})
      
      data_to_save_list <- list( timestamp = Sys.time(), analysis_type = "ACP", selected_vars = I(input$vars_select), selected_axes = current_axes, key_results_eigenvalues = as.data.frame(res.pca$eig), conclusion = conclusion_text, status = 'Pending Approval', analyst_id = Sys.getenv("USER", "unknown") )
      
      con_decis_mongo <- get_decis_mongo_connection()
      if (is.null(con_decis_mongo)) {
        # removeNotification("saving_pca") # Handled by on.exit
        showNotification("Erreur Connexion DB Décisionnelle (Mongo).", type = "error"); return()
      }
      
      tryCatch({
        # Corrected insert call
        insert_result <- con_decis_mongo$insert(data_to_save_list)
        
        # removeNotification handled by on.exit
        # Success notification
        showNotification("Analyse ACP soumise avec succès!", type = "message", duration = 5)
        
        # Corrected update call (added ns() for namespace)
        updateTextAreaInput(session, ns("conclusion_pca"), value = "")
        
      }, error = function(e) {
        # removeNotification handled by on.exit
        message("MongoDB Save Error: ", e$message)
        showNotification(paste("Erreur sauvegarde DB (Mongo):", e$message), type = "error", duration = 10)
      })     
      
    })# End observeEvents
    return(acp_results) # Return the reactive itself
    
  })
}