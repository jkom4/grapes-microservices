# modules/mod_clustering.R

# --- Libraries ---
library(shiny)
library(FactoMineR) # For HCPC
library(factoextra) # For cluster visualizations
library(dplyr)
library(ggplot2)
library(DT)
library(shinycssloaders)
library(writexl)
library(jsonlite)
library(DBI)
# library(RMySQL) # Or RMariaDB - Not needed here if using Mongo for save
library(mongolite) # Needed for saving
library(tibble)

# Note: Depends on global.R for DB connection helpers

# --- UI Function ---
mod_clustering_ui <- function(id) {
  ns <- NS(id)
  tagList(
    h3("Hierarchical Agglomerative Clustering (HAC / HCPC)"),
    fluidRow(
      column(width = 3,
             wellPanel(
               h4("HCPC Options"),
               helpText("Classification based on the results of the previous PCA (principal dimensions)."),
               numericInput(ns("hcpc_n_dim"), "Number of PCA Dimensions to Use:", value = 5, min = 2, step = 1),
               numericInput(ns("hcpc_n_clust"), "Number of Clusters (0 = auto):", value = 0, min = 0, step = 1),
               actionButton(ns("run_hcpc"), "Run/Update Clustering")
             ),
             wellPanel(
               h4("Actions"),
               textAreaInput(ns("conclusion_hcpc"), "Cluster Conclusion / Interpretation:", rows = 4),
               actionButton(ns("save_hcpc"), "Submit Clustering Analysis", icon = icon("database")),
               hr(),
               h5("Download Results:"),
               downloadButton(ns("dl_hcpc_dendro"), "Dendrogram (.png)"),
               downloadButton(ns("dl_hcpc_map"), "Cluster Factor Map (.png)"),
               downloadButton(ns("dl_hcpc_desc"), "Cluster Description (.xlsx)")
               # Potentially add download for clustered data
               # downloadButton(ns("dl_hcpc_data_clust"), "Data with Clusters (.xlsx)")
             )
      ),
      column(width = 9,
             h3("HCPC Results"),
             tabsetPanel(
               tabPanel("Factor Map", withSpinner(plotOutput(ns("hcpc_map_plot")))),
               tabPanel("Dendrogram", withSpinner(plotOutput(ns("hcpc_dendro_plot")))),
               # <<< Reverted back to verbatimTextOutput >>>
               tabPanel("Cluster Description (Quantitative Variables)",
                        withSpinner(verbatimTextOutput(ns("hcpc_desc_quanti")))
               ),
               # <<< End Revert >>>
               tabPanel("Data with Clusters", withSpinner(DT::dataTableOutput(ns("hcpc_data_clust"))))
             ) # End tabsetPanel
      ) # End column
    ) # End fluidRow
  ) # End tagList
}


# --- Server Function ---
mod_clustering_server <- function(id, acp_results_reactive) {
  moduleServer(id, function(input, output, session) {
    ns <- session$ns
    
    # --- Reactive for HCPC results ---
    # Reactive for HCPC results
    hcpc_results <- eventReactive(input$run_hcpc, {
      res.pca <- acp_results_reactive(); req(res.pca, inherits(res.pca, "PCA"))
      nb_dim_requested <- input$hcpc_n_dim; nb_clust <- input$hcpc_n_clust
      max_dim_pca <- tryCatch(ncol(res.pca$eig), error = function(e) 0)
      # ... validation req(...) calls ...
      nb_dim_to_use <- nb_dim_requested
      
      # +++ DEBUG +++
      message("--- HCPC Inputs ---")
      message("Number of dimensions requested (nb_dim_to_use): ", nb_dim_to_use)
      # Get number of dimensions actually available in PCA coords
      actual_pca_dims <- tryCatch(ncol(res.pca$ind$coord), error=function(e) 0)
      message("Actual dimensions available in PCA (ind$coord): ", actual_pca_dims)
      # Check if requested <= available
      if (nb_dim_to_use > actual_pca_dims) {
        message("!!! WARNING: Requested dims > available dims !!!")
      }
      message("--------------------")
      # +++ END DEBUG +++
      
      
      showNotification("Calcul HCPC...", type="message", duration=3)
      result <- tryCatch({
        FactoMineR::HCPC(res.pca,
                         nb.clust = ifelse(nb_clust == 0, -1, nb_clust),
                         consol = TRUE, graph = FALSE, metric = "euclidean",
                         method = "ward", nb.par = nb_dim_to_use )
      }, error = function(e) {
        showNotification(paste("Erreur HCPC:", e$message), type="error", duration=10); return(NULL)
      })
      req(result, .label="HCPC Calculation Result"); # Ensure HCPC didn't return NULL
      return(result)
    }, ignoreNULL = TRUE)
    
    # --- Plot Objects ---
    hcpc_map_plot_obj <- reactive({ req(hcpc_results()); axes_choice <- c(1, 2); fviz_cluster(hcpc_results(), axes = axes_choice, geom = "point", repel = TRUE, show.clust.cent = TRUE, palette = "jco", ggtheme = theme_minimal(), main = paste("Clusters sur Plan Factoriel ACP (Axes", axes_choice[1], "&", axes_choice[2], ")") ) })
    hcpc_dendro_plot_obj <- reactive({ req(hcpc_results()); fviz_dend(hcpc_results(), show_labels = FALSE, palette = "jco", main = "Dendrogramme HCPC") + theme(axis.text.y=element_blank(), axis.ticks.y=element_blank()) })
    
    # --- Table Data Reactives ---
    hcpc_desc_quanti_reactive_rounded <- reactive({
      # Reactive remains the same - calculates rounded data + Variable_Cluster
      res.hcpc <- hcpc_results(); req(res.hcpc)
      desc_raw <- NULL
      tryCatch({ if (!is.null(res.hcpc$desc.var$quanti.var)) { desc_raw <- as.data.frame(res.hcpc$desc.var$quanti.var) } else if (!is.null(res.hcpc$desc.var$quanti)) { desc_raw <- as.data.frame(res.hcpc$desc.var$quanti) } else { return(NULL) } }, error = function(e) { return(NULL) })
      req(desc_raw)
      desc_with_rownames <- tryCatch({ desc_raw %>% tibble::rownames_to_column("Variable_Cluster") }, error = function(e) { return(NULL) })
      req(desc_with_rownames)
      df_to_round <- desc_with_rownames; numeric_col_names <- names(df_to_round)[sapply(df_to_round, is.numeric)];
      for (col_name in numeric_col_names) { tryCatch({ df_to_round[[col_name]] <- round(df_to_round[[col_name]], 3) }, error = function(e) { warning(paste("Could not round:", col_name, "Error:", e$message)) }) }
      return(df_to_round)
    })
    hcpc_data_clust_reactive <- reactive({ res.hcpc <- hcpc_results(); req(res.hcpc); clust_data <- tryCatch(as_tibble(res.hcpc$data.clust), error = function(e) NULL); req(clust_data); return(clust_data) })
    
    
    # --- Render Plots (Uncommented) ---
    output$hcpc_map_plot <- renderPlot({ req(hcpc_map_plot_obj()); hcpc_map_plot_obj() })
    output$hcpc_dendro_plot <- renderPlot({ req(hcpc_dendro_plot_obj()); hcpc_dendro_plot_obj() })
    
    # --- Render Tables ---
    
    # <<< Reverted back to renderPrint >>>
    output$hcpc_desc_quanti <- renderPrint({
      df_to_render <- hcpc_desc_quanti_reactive_rounded()
      req(df_to_render)
      # Use capture.output + print for reliable text rendering
      output_string <- capture.output(print(df_to_render, print.gap = 3, right = TRUE, max = 9999))
      cat(output_string, sep = "\n")
    })
    # <<< End Revert >>>
    
    output$hcpc_data_clust <- DT::renderDataTable({
      req(hcpc_data_clust_reactive())
      DT::datatable(hcpc_data_clust_reactive() %>% head(50),
                    rownames = FALSE,
                    options = list(scrollX = TRUE, scrollY = "400px", paging=FALSE, searching=FALSE, autoWidth = TRUE))
    })
    # --- ADD temporary output for full description ---
    output$debug_hcpc_desc_var <- renderPrint({
      res.hcpc <- hcpc_results()
      req(res.hcpc)
      cat("--- Raw hcpc_results$desc.var Output ---\n")
      print(res.hcpc$desc.var) # Print the entire description list
      cat("\n--- Specifically Quanti Part ---\n")
      # Try accessing both common names for the quantitative part
      if(!is.null(res.hcpc$desc.var$quanti.var)) print(res.hcpc$desc.var$quanti.var)
      if(!is.null(res.hcpc$desc.var$quanti)) print(res.hcpc$desc.var$quanti)
    })
    
    # (Debug Observer - Can be commented out or removed now)
    # observe({ data_to_inspect <- hcpc_desc_quanti_reactive_rounded(); req(data_to_inspect); message("------ HCPSC Desc Quanti Reactive Result (Console Print) ------"); print(data_to_inspect); message("------ End HCPSC Desc Quanti Reactive Result ------") })
    
    # --- Download Handlers ---
    # (Download handlers remain the same, using hcpc_desc_quanti_reactive_rounded for the desc table)
    output$dl_hcpc_map <- downloadHandler( filename = function() { paste0('hcpc_cluster_map_', Sys.Date(), '.png') }, content = function(file) { req(hcpc_map_plot_obj()); ggsave(file, plot = hcpc_map_plot_obj(), device = "png", width = 8, height = 7) } )
    output$dl_hcpc_dendro <- downloadHandler( filename = function() { paste0('hcpc_dendrogram_', Sys.Date(), '.png') }, content = function(file) { req(hcpc_dendro_plot_obj()); ggsave(file, plot = hcpc_dendro_plot_obj(), device = "png", width = 8, height = 6) } )
    output$dl_hcpc_desc <- downloadHandler( filename = function() { paste0('hcpc_description_', Sys.Date(), '.xlsx') }, content = function(file) { req(hcpc_desc_quanti_reactive_rounded()); writexl::write_xlsx( list(Description_Quanti = hcpc_desc_quanti_reactive_rounded()), file ) } )
    
    
    # --- Save to DB Logic ---
    # (Save logic remains the same)
    observeEvent(input$save_hcpc, { req(hcpc_results(), input$conclusion_hcpc); conclusion_text <- trimws(input$conclusion_hcpc); if(nchar(conclusion_text) < 5){ showNotification("La conclusion doit contenir au moins 5 caractères.", type = "warning", duration=5); return() }; showNotification("Soumission en cours...", type="message", duration = NULL, id="saving_hcpc"); on.exit(removeNotification("saving_hcpc"), add = TRUE); res.hcpc <- hcpc_results(); current_axes <- tryCatch(selected_axes(), error=function(e) NULL); req(current_axes); axes_str <- paste(current_axes, collapse = ","); cluster_counts <- tryCatch(table(res.hcpc$data.clust$clust), error=function(e) NA); results_summary <- list( n_dim_used = input$hcpc_n_dim, n_clust_chosen = input$hcpc_n_clust, n_clust_found = length(levels(res.hcpc$data.clust$clust)), cluster_counts = as.list(cluster_counts) ); key_results_json <- tryCatch(toJSON(results_summary, auto_unbox=TRUE), error=function(e) NA_character_); data_to_save_list <- list( timestamp = Sys.time(), analysis_type = "HCPC", selected_vars = "Based on previous ACP", selected_axes = paste0("ACP Dims 1-", input$hcpc_n_dim), key_results = key_results_json, conclusion = conclusion_text, status = 'Pending Approval', analyst_id = Sys.getenv("USER", "unknown") ); con_decis_mongo <- get_decis_mongo_connection(); if (is.null(con_decis_mongo)) { showNotification("Erreur DB Décisionnelle (Mongo).", type = "error"); return() }; tryCatch({ insert_result <- con_decis_mongo$insert(data_to_save_list); showNotification("Analyse HCPC soumise (Mongo)!", type = "message", duration = 5); updateTextAreaInput(session, ns("conclusion_hcpc"), value = "") }, error = function(e) { message("MongoDB Save Error (HCPC): ", e$message); showNotification(paste("Erreur sauvegarde DB (Mongo):", e$message), type = "error", duration = 10) }) })
    
    # --- Return Value for the Module ---
    # Return a LIST containing both the HCPC result and the clustered data
    return(
      list(
        results = hcpc_results, # The main reactive returning the HCPC object
        data_clust = hcpc_data_clust_reactive # The reactive returning data with cluster assignments
      )
    )
    
    
  }) # End moduleServer
}