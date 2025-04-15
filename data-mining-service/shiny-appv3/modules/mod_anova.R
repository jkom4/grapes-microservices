# modules/mod_anova.R

library(shiny)
library(dplyr)
library(ggplot2)
library(DT)
library(broom)
library(shinycssloaders)
library(writexl)
library(jsonlite)

# UI Function
mod_anova_ui <- function(id) {
  ns <- NS(id)
  tagList(
    h2("Analyse de la Variance (ANOVA)"),
    fluidRow(
      column(4,
             wellPanel(
               h4("Options ANOVA"), # Added H4 title
               selectInput(ns("quant_var"), "Variable Quantitative:", choices = NULL),
               selectInput(ns("cat_var"), "Variable Catégorielle:", choices = NULL),
               # <<< ADD helpText regarding cluster >>>
               helpText(em("Note: Pour executer ANOVA, exécuter le module 'Clustering (CAH)' d'abord.")),
               # <<< END ADD >>>
               checkboxInput(ns("run_tukey"), "Inclure test post-hoc Tukey", value = FALSE),
               actionButton(ns("run_anova"), "Exécuter ANOVA", icon = icon("play")),
               hr(), # Added hr for separation
               h4("Sauvegarde"), # Added H4 title
               textAreaInput(ns("conclusion"), "Conclusion/Interprétation:", "", rows = 3),
               actionButton(ns("save_anova"), "Soumettre Analyse", icon = icon("save")), # Changed label slightly
               hr(), # Added hr
               h5("Télécharger"), # Changed to H5
               downloadButton(ns("download_summary"), "Exporter Résumé (.txt)"), # Added file type
               downloadButton(ns("download_boxplot"), "Exporter Boxplot (.png)") # Added file type
             )
      ),
      column(8,
             h3("Résultats ANOVA"), # Moved H3 inside column
             tabsetPanel(
               tabPanel("Résumé ANOVA", verbatimTextOutput(ns("anova_summary")) %>% withSpinner()),
               tabPanel("Boxplot", plotOutput(ns("anova_boxplot")) %>% withSpinner()),
               tabPanel("Moyennes Groupes", DTOutput(ns("group_means_table")) %>% withSpinner()),
               # ConditionalPanel requires the tabPanel INSIDE it
               conditionalPanel( condition = paste0("input['", ns("run_tukey"), "'] == true"),
                                 tabPanel("Tukey HSD", DTOutput(ns("tukey_results_table")) %>% withSpinner())
               )
             )
      ) # End Results column
    ) # End FluidRow
  ) # End tagList
}

# Server Function
mod_anova_server <- function(id, data_reactive, clustered_data_reactive) {
  moduleServer(id, function(input, output, session) {
    ns <- session$ns
    `%||%` <- function(a, b) if (!is.null(a)) a else b
    rv <- reactiveValues(results = NULL)
    
    # Update dropdowns when data is ready
    observe({
      df <- data_reactive()
      df_clust <- clustered_data_reactive() %||% NULL
      req(df)
      
      quant_vars <- df %>%
        select(where(is.numeric)) %>%
        select(-any_of(c("client_id", "product_id", "service_id", "transaction_id"))) %>%
        names()
      
      cat_vars <- df %>%
        select(where(~is.factor(.) || is.character(.))) %>%
        select(-any_of(c("client_id", "product_id"))) %>%
        names()
      
      if (!is.null(df_clust) && "clust" %in% names(df_clust)) {
        cat_vars <- c("clust", cat_vars)
      }
      
      updateSelectInput(session, "quant_var", choices = quant_vars, selected = quant_vars[1])
      updateSelectInput(session, "cat_var", choices = cat_vars, selected = cat_vars[1])
    })
    
    # Run ANOVA
    observeEvent(input$run_anova, {
      req(input$quant_var, input$cat_var)
      df <- data_reactive()
      df_clust <- clustered_data_reactive() %||% NULL
      
      if (input$cat_var == "clust" && (is.null(df_clust) || !"clust" %in% names(df_clust))) {
        showNotification("⚠️ Veuillez exécuter le module de clustering avant d’utiliser les clusters dans l’ANOVA.", type = "error", duration = 6)
        return()
      }
      
      df_model <- if (input$cat_var == "clust") {
        df_clust %>% select(quant_var = all_of(input$quant_var), cat_var = clust)
      } else {
        df %>% select(quant_var = all_of(input$quant_var), cat_var = all_of(input$cat_var))
      }
      
      df_model <- df_model %>% drop_na() %>% mutate(cat_var = as.factor(cat_var))
      
      if (nrow(df_model) < 2 || n_distinct(df_model$cat_var) < 2) {
        showNotification("Pas assez de données/groupes pour l'ANOVA.", type = "warning")
        return()
      }
      
      aov_model <- aov(quant_var ~ cat_var, data = df_model)
      tukey_res <- NULL
      
      if (input$run_tukey && summary(aov_model)[[1]]$`Pr(>F)`[1] < 0.05) {
        tukey_res <- TukeyHSD(aov_model, "cat_var")
      }
      
      rv$results <- list(
        model = aov_model,
        summary = summary(aov_model),
        tukey = tukey_res,
        data_used = df_model,
        var_q = input$quant_var,
        var_c = input$cat_var
      )
    })
    
    # Outputs
    output$anova_summary <- renderPrint({ req(rv$results); print(rv$results$summary) })
    
    output$group_means_table <- renderDT({
      req(rv$results)
      df <- rv$results$data_used
      df %>%
        group_by(cat_var) %>%
        summarise(
          Moyenne = round(mean(quant_var), 2),
          Écart_type = round(sd(quant_var), 2),
          .groups = "drop"
        ) %>% datatable()
    })
    
    output$anova_boxplot <- renderPlot({
      req(rv$results)
      ggplot(rv$results$data_used, aes(x = cat_var, y = quant_var, fill = cat_var)) +
        geom_boxplot() +
        labs(title = paste("Boxplot:", rv$results$var_q, "par", rv$results$var_c)) +
        theme_minimal()
    })
    
    output$tukey_results_table <- renderDT({
      req(rv$results$tukey)
      broom::tidy(rv$results$tukey) %>%
        mutate(across(where(is.numeric), round, 4)) %>%
        datatable()
    })
    
    # Downloads
    output$download_summary <- downloadHandler(
      filename = function() {
        paste0("anova_summary_", rv$results$var_q, "_by_", rv$results$var_c, ".txt")
      },
      content = function(file) {
        sink(file)
        print(rv$results$summary)
        if (!is.null(rv$results$tukey)) {
          cat("\n--- Tukey HSD ---\n")
          print(rv$results$tukey)
        }
        sink()
      }
    )
    
    output$download_boxplot <- downloadHandler(
      filename = function() {
        paste0("anova_boxplot_", rv$results$var_q, "_by_", rv$results$var_c, ".png")
      },
      content = function(file) {
        ggsave(file, plot = {
          ggplot(rv$results$data_used, aes(x = cat_var, y = quant_var, fill = cat_var)) +
            geom_boxplot() + theme_minimal()
        }, width = 8, height = 6)
      }
    )
    
    # --- MongoDB Save ---
    observeEvent(input$save_anova, {
      req(rv$results, input$conclusion)
      
      conclusion_text <- trimws(input$conclusion)
      if (nchar(conclusion_text) < 5) {
        showNotification("Merci d'écrire une conclusion d'au moins 5 caractères.", type = "warning")
        return()
      }
      
      summary_aov <- rv$results$summary
      p_value <- tryCatch(summary_aov[[1]]$`Pr(>F)`[1], error = function(e) NA)
      f_stat <- tryCatch(summary_aov[[1]]$`F value`[1], error = function(e) NA)
      
      key_results <- list(
        dependent_var = rv$results$var_q,
        grouping_var = rv$results$var_c,
        F_statistic = f_stat,
        p_value = p_value,
        tukey_used = !is.null(rv$results$tukey)
      )
      
      data_to_save <- list(
        timestamp = Sys.time(),
        analysis_type = "ANOVA",
        selected_vars = paste(rv$results$var_q, "~", rv$results$var_c),
        selected_axes = NA_character_,
        key_results = jsonlite::toJSON(key_results, auto_unbox = TRUE),
        conclusion = conclusion_text,
        status = "Pending Approval",
        analyst_id = Sys.getenv("USER", "unknown")
      )
      
      con_decis_mongo <- tryCatch(get_decis_mongo_connection(), error = function(e) NULL)
      
      if (is.null(con_decis_mongo)) {
        showNotification("Connexion à MongoDB échouée.", type = "error")
        return()
      }
      
      tryCatch({
        con_decis_mongo$insert(data_to_save)
        showNotification("Analyse ANOVA soumise avec succès !", type = "message", duration = 5)
        updateTextAreaInput(session, ns("conclusion"), value = "")
      }, error = function(e) {
        message("Erreur MongoDB (ANOVA): ", e$message)
        showNotification(paste("Erreur lors de l'enregistrement :", e$message), type = "error")
      })
    })
    
  }) # end moduleServer
}
