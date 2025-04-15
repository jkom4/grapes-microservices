# modules/mod_regression.R

# --- Libraries ---
library(shiny)
library(dplyr)
library(ggplot2)
library(DT)
library(shinycssloaders)
library(broom)     # Optional: For tidying model output
library(writexl)   # For download
library(jsonlite)  # For saving results
# DBI, RMySQL/RMariaDB needed for save if not relying on global
# mongolite if saving to mongo

# Note: Depends on global.R for DB connection helpers if saving is implemented here

# --- UI Function ---
mod_regression_ui <- function(id) {
  ns <- NS(id)
  tagList(
    h3("Modélisation par Régression"),
    fluidRow(
      # --- Options Column ---
      column(width = 3,
             wellPanel(
               h4("Options Régression"),
               radioButtons(ns("model_type"), "Type de Régression:",
                            choices = list("Linéaire (Variable quantitative)" = "linear",
                                           "Logistique (Variable binaire 0/1)" = "logistic"),
                            selected = "linear"),
               hr(),
               # Dependent Variable (Choices updated dynamically)
               selectInput(ns("vars_select_dependent"), "Choisir la Variable Dépendante (Y):",
                           choices = NULL),
               hr(),
               # Independent Variables (Choices updated dynamically)
               selectInput(ns("vars_select_independent"), "Choisir les Variables Indépendantes (X):",
                           choices = NULL, multiple = TRUE, selected = NULL),
               hr(),
               actionButton(ns("run_regression"), "Lancer/Actualiser Régression")
             ),
             wellPanel(
               h4("Actions"),
               textAreaInput(ns("conclusion_reg"), "Conclusion / Interprétation:", rows = 4),
               actionButton(ns("save_reg"), "Soumettre l'analyse Régression", icon = icon("database")),
               hr(),
               h5("Télécharger les Résultats:"),
               downloadButton(ns("dl_reg_summary"), "Résumé Modèle (.txt)"),
               downloadButton(ns("dl_reg_data"), "Données Utilisées (.xlsx)")
               # Add download for plots later if needed
             )
      ), # End Options Column
      
      # --- Results Column ---
      column(width = 9,
             h3("Résultats Régression"),
             tabsetPanel(
               tabPanel("Résumé du Modèle",
                        helpText("Interprétation: Significativité des variables (Pr(>|t|)), qualité d'ajustement (R-squared / AIC), etc."),
                        withSpinner(verbatimTextOutput(ns("model_summary")))
               ),
               # Conditional panel for Linear Regression Diagnostics
               conditionalPanel(
                 condition = paste0("input['", ns("model_type"), "'] == 'linear'"),
                 tabPanel("Diagnostiques Linéaire",
                          helpText("Graphiques standards pour vérifier les hypothèses de la régression linéaire (linéarité, homoscédasticité, normalité des résidus, outliers)."),
                          withSpinner(plotOutput(ns("diagnostic_plots")))
                 )
               ),
               # Conditional panel for Logistic Regression Extras
               conditionalPanel(
                 condition = paste0("input['", ns("model_type"), "'] == 'logistic'"),
                 tabPanel("Extras Logistique",
                          helpText("Probabilités prédites vs Réalité, ou autres métriques spécifiques."),
                          # Placeholder for future plots/tables (e.g., ROC, Conf Matrix)
                          withSpinner(verbatimTextOutput(ns("logistic_extras")))
                 )
               ),
               tabPanel("Données Utilisées",
                        helpText("Aperçu des données après traitement NA, utilisées pour entraîner le modèle."),
                        withSpinner(DT::dataTableOutput(ns("head_data_regression")))
               )
             ) # End tabsetPanel
      ) # End Results Column
    ) # End fluidRow
  ) # End tagList
}

# --- Server Function ---
mod_regression_server <- function(id, data_reactive) { # Takes FULL enriched data
  moduleServer(id, function(input, output, session) {
    ns <- session$ns
    
    # --- Dynamic UI Updates ---
    
    # Update DEPENDENT variable choices based on model type
    observeEvent(list(data_reactive(), input$model_type), {
      df <- data_reactive()
      req(df)
      if (input$model_type == "linear") {
        # Choices for Linear: Numeric variables
        choices_dep <- names(df)[sapply(df, is.numeric)]
      } else { # Logistic
        # Choices for Logistic: Factor with 2 levels, logical, or numeric 0/1
        choices_dep <- names(df)[sapply(df, function(col) {
          (is.factor(col) && nlevels(col) == 2) || is.logical(col) || (is.numeric(col) && all(na.omit(col) %in% c(0, 1)))
        })]
      }
      updateSelectInput(session, "vars_select_dependent", choices = choices_dep, selected=ifelse(length(choices_dep)>0, choices_dep[1], ""))
    })
    
    # Update INDEPENDENT variable choices (allow numeric & factors/characters)
    observeEvent(data_reactive(), {
      df <- data_reactive()
      req(df)
      # Allow numeric, factor, character (convert char to factor later)
      choices_indep <- names(df)[sapply(df, function(col) is.numeric(col) || is.factor(col) || is.character(col))]
      # Exclude potential ID columns or the selected dependent variable
      current_dep_var <- input$vars_select_dependent
      choices_indep <- choices_indep[!choices_indep %in% c("client_id", "product_id", "service_id", current_dep_var)] # Example exclusion
      updateSelectInput(session, "vars_select_independent", choices = choices_indep, selected = NULL)
    })
    # Re-update independent choices if dependent changes (to exclude it)
    observeEvent(input$vars_select_dependent,{
      df <- data_reactive()
      req(df)
      choices_indep <- names(df)[sapply(df, function(col) is.numeric(col) || is.factor(col) || is.character(col))]
      current_dep_var <- input$vars_select_dependent
      choices_indep <- choices_indep[!choices_indep %in% c("client_id", "product_id", "service_id", current_dep_var)]
      # Keep current selection if possible
      selected_indep <- input$vars_select_independent
      selected_indep <- selected_indep[selected_indep %in% choices_indep] # Remove dependent if selected
      updateSelectInput(session, "vars_select_independent", choices = choices_indep, selected = selected_indep)
    })
    
    
    # --- Data Preparation Reactive ---
    data_for_regression <- eventReactive(input$run_regression, {
      df_full <- data_reactive()
      dep_var <- input$vars_select_dependent
      indep_vars <- input$vars_select_independent
      
      # Use req for initial input checks
      req(df_full, dep_var, indep_vars, length(indep_vars) >= 1,
          .label = "Regression Input Variables")
      selected_vars <- c(dep_var, indep_vars)
      req(all(selected_vars %in% names(df_full)), .label = "Regression Variable Exists Check")
      
      # Subset data and handle NAs
      df_subset_initial <- df_full %>% select(all_of(selected_vars))
      n_initial <- nrow(df_subset_initial)
      df_subset <- df_subset_initial %>% na.omit()
      n_complete <- nrow(df_subset)
      
      if (n_complete < n_initial) {
        showNotification(paste("Note:", n_initial - n_complete, "lignes avec NAs ont été supprimées."),
                         type="warning", duration=5)
      }
      
      # --- FIX: Replace validate with req or if/showNotification ---
      # Use req to ensure enough rows remain AFTER NA removal
      req(nrow(df_subset) > length(indep_vars) + 1,
          .label = "Regression Complete Data Rows Check")
      # --- End FIX ---
      
      
      # Prepare/Check dependent variable for Logistic Regression
      if (input$model_type == "logistic") {
        is_valid_logistic <- FALSE
        y_col <- df_subset[[dep_var]] # Extract column once
        
        if (is.logical(y_col)) {
          df_subset[[dep_var]] <- factor(y_col, levels = c(FALSE, TRUE))
          is_valid_logistic <- TRUE
        } else if (is.numeric(y_col) && all(na.omit(y_col) %in% c(0, 1))) {
          df_subset[[dep_var]] <- factor(y_col, levels = c(0, 1))
          is_valid_logistic <- TRUE
        } else if (is.factor(y_col) && nlevels(y_col) == 2) {
          message("Using existing 2-level factor for logistic DV.")
          is_valid_logistic <- TRUE
        }
        
        # --- FIX: Use 'if' check + showNotification + req(FALSE) ---
        if (!is_valid_logistic) {
          showNotification("Variable Dépendante Logistique non valide (doit être Binaire 0/1, TRUE/FALSE, ou facteur à 2 niv.).", type="error", duration=10)
          req(FALSE, .label = "Invalid Logistic DV Type") # Stop reactive execution
        }
        # --- End FIX ---
      }
      
      # Convert characters to factors for independent variables
      df_subset <- df_subset %>%
        mutate(across(where(is.character), as.factor))
      
      message("Regression data prepared.") # DEBUG confirmation
      return(list(data = df_subset, dep_var = dep_var, indep_vars = indep_vars, type=input$model_type))
      
    }, ignoreNULL = TRUE)
    
    # --- Model Fitting Reactive ---
    regression_model <- reactive({
      prep_list <- data_for_regression()
      req(prep_list, prep_list$data) # Require prepared data
      
      df_model <- prep_list$data
      dep_var <- prep_list$dep_var
      indep_vars <- prep_list$indep_vars
      model_type <- prep_list$type
      
      # Construct formula
      formula_str <- paste0("`", dep_var, "` ~ ", paste(paste0("`", indep_vars, "`"), collapse = " + ")) # Use backticks for non-standard names
      model_formula <- as.formula(formula_str)
      
      message("Fitting Regression Model: ", formula_str) # DEBUG
      showNotification("Ajustement du modèle...", duration=2)
      
      # Fit model
      model <- tryCatch({
        if(model_type == "linear") {
          lm(model_formula, data = df_model)
        } else { # logistic
          glm(model_formula, data = df_model, family = binomial(link = "logit"))
        }
      }, error = function(e) {
        showNotification(paste("Erreur de Modélisation:", e$message), type = "error", duration = 10)
        return(NULL)
      })
      req(model) # Require successful model fit
      return(model)
    })
    
    
    # --- Render Outputs ---
    
    # Model Summary
    output$model_summary <- renderPrint({
      model <- regression_model()
      req(model) # Require model object
      summary(model)
    })
    
    # Diagnostic Plots (Linear only)
    output$diagnostic_plots <- renderPlot({
      model <- regression_model()
      req(model, input$model_type == "linear") # Require linear model
      par(mfrow=c(2,2)) # Setup 2x2 plot layout
      plot(model)       # Generate base R diagnostic plots
      par(mfrow=c(1,1)) # Reset plot layout
    })
    
    # Logistic Extras
    output$logistic_extras <- renderPrint({
      model <- regression_model()
      req(model, input$model_type == "logistic") # Require logistic model
      cat("Coefficients:\n")
      print(coef(model))
      cat("\nOdds Ratios (exponentiated coefficients):\n")
      # Use tryCatch in case exp() fails
      tryCatch({ print(exp(coef(model))) }, error=function(e){ cat("Erreur calcul Odds Ratios.\n")})
      # Add confusion matrix or other metrics here later if desired
    })
    
    # Data Preview Table
    output$head_data_regression <- DT::renderDataTable({
      prep_list <- data_for_regression() # Get data used for model
      req(prep_list, prep_list$data)
      DT::datatable(head(prep_list$data, 50), rownames = FALSE,
                    options = list(scrollX = TRUE, scrollY = "250px", paging = FALSE, searching = FALSE))
    })
    
    # --- Download Handlers ---
    output$dl_reg_summary <- downloadHandler(
      filename = function() { paste0('regression_', input$model_type, '_summary_', Sys.Date(), '.txt') },
      content = function(file) {
        req(regression_model())
        sink(file) # Redirect output to file
        cat(paste("Regression Model Type:", input$model_type, "\n"))
        cat(paste("Dependent Variable:", input$vars_select_dependent, "\n"))
        cat(paste("Independent Variables:", paste(input$vars_select_independent, collapse=", "), "\n\n"))
        print(summary(regression_model()))
        # Add logistic extras if applicable
        if(input$model_type == "logistic"){
          cat("\n\nOdds Ratios:\n")
          tryCatch({ print(exp(coef(regression_model()))) }, error=function(e){ cat("NA\n")})
        }
        sink() # Turn output redirection off
      }
    )
    
    output$dl_reg_data <- downloadHandler(
      filename = function() { paste0('regression_data_used_', Sys.Date(), '.xlsx') },
      content = function(file) {
        req(data_for_regression()) # Get the data used for the model
        writexl::write_xlsx( list(Data = data_for_regression()$data), file )
      }
    )
    
    # --- Save to DB Logic ---
    observeEvent(input$save_reg, {
      # Needs model results AND prepared data list to get vars
      req(regression_model(), data_for_regression(), input$conclusion_reg, nchar(trimws(input$conclusion_reg)) >= 5)
      
      model <- regression_model()
      prep_list <- data_for_regression()
      conclusion_text <- trimws(input$conclusion_reg)
      
      # Summarize results (e.g., R^2 or AIC, coefficients)
      results_summary <- tryCatch({
        if(prep_list$type == "linear"){
          s <- summary(model)
          list(R_squared = s$r.squared, Adj_R_squared = s$adj.r.squared, AIC = AIC(model), Coeffs = coef(model))
        } else { # Logistic
          list(AIC = AIC(model), Deviance = model$deviance, Null_Deviance = model$null.deviance, Coeffs=coef(model), Odds_Ratios = exp(coef(model)))
        }
      }, error = function(e){ list(error = e$message) })
      
      
      data_to_save_list <- list(
        timestamp = Sys.time(),
        analysis_type = paste0("Regression-", prep_list$type),
        # Save formula or var names
        selected_vars = paste0(prep_list$dep_var, "~", paste(prep_list$indep_vars, collapse="+")),
        selected_axes = NA_character_, # Not applicable
        key_results = toJSON(results_summary, auto_unbox=TRUE, pretty = FALSE), # Save summary as JSON
        conclusion = conclusion_text,
        status = 'Pending Approval',
        analyst_id = Sys.getenv("USER", "unknown")
      )
      
      con_decis_mongo <- get_decis_mongo_connection()
      if (is.null(con_decis_mongo)) { showNotification("Erreur DB Décisionnelle.", type = "error"); return() }
      
      tryCatch({
        insert_result <- con_decis_mongo$insert(data_to_save_list)
        showNotification("Analyse Régression soumise (Mongo)!", type = "message", duration = 5)
        updateTextAreaInput(session, ns("conclusion_reg"), value = "")
      }, error = function(e) {
        message("MongoDB Save Error (Regression): ", e$message)
        showNotification(paste("Erreur sauvegarde DB:", e$message), type = "error", duration = 10)
      })
    })
    
    
  }) # End moduleServer
}