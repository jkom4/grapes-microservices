# server.R

# --- Libraries ---
library(shiny)
library(dplyr)
library(lubridate)

# --- Source Helper Scripts & Modules ---
source("global.R") # Source global first
source("load_data.R")
source("modules/mod_overview.R")
source("modules/mod_acp.R")
source("modules/mod_acm.R")
source("modules/mod_clustering.R")
source("modules/mod_regression.R")
source("modules/mod_anova.R")
source("modules/mod_chisq.R")
source("modules/mod_validation.R") # Ensure validation module is sourced

# --- Server Function Definition ---
server <- function(input, output, session) {
  
  # --- Data Loading and Reactives ---
  # Define the helper function for safe NULL handling
  `%||%` <- function(a, b) if (!is.null(a)) a else b
  
  data_loaded <- reactiveVal(NULL)
  observeEvent(TRUE, {
    showModal(modalDialog("Chargement...", footer=NULL))
    loaded_list <- tryCatch({ load_and_prepare_data() },
                            error = function(e){ message("ERROR in load_data: ", e$message); showNotification(paste("Échec critique:", e$message), type="error"); NULL})
    removeModal()
    if (!is.null(loaded_list)) {
      data_loaded(loaded_list)
      # Check usability... (keep existing checks)
      qc_ok <- !is.null(loaded_list$quantitative_data) && nrow(loaded_list$quantitative_data %||% data.frame()) > 1 && ncol(loaded_list$quantitative_data %||% data.frame()) > 1
      ql_ok <- !is.null(loaded_list$qualitative_data) && nrow(loaded_list$qualitative_data %||% data.frame()) > 1 && ncol(loaded_list$qualitative_data %||% data.frame()) > 1
      showNotification(paste("Données chargées.", ifelse(qc_ok, "ACP prête.", ""), ifelse(ql_ok, "ACM prête.", "")), type = "message", duration=5)
    }
    # Error notification handled in tryCatch
  }, once = TRUE)
  
  # --- Define Reactives ---
  r_full_data <- reactive({ req(data_loaded()); data_loaded()$full_sampled_data %||% tibble() })
  r_quantitative_data <- reactive({ data_loaded()$quantitative_data }) # Modules using this should use req()
  r_qualitative_data <- reactive({ data_loaded()$qualitative_data })  # Modules using this should use req()
  r_auth_summary <- reactive({ data_loaded()$auth_summary })        # Modules using this should use req()
  r_service_summary <- reactive({ data_loaded()$service_summary }) # Modules using this should use req()
  
  
  # --- Call Module Servers ---
  
  # Overview
  mod_overview_server(
    id = "overview_module",
    data_reactive = r_full_data,
    auth_summary_reactive = r_auth_summary,
    service_summary_reactive = r_service_summary
  )
  
  # ACP (returns results reactive)
  acp_results_reactive_from_module <- mod_acp_server(
    id = "acp_module",
    data_reactive = r_quantitative_data
  )
  
  # ACM
  mod_acm_server(
    id = "acm_module",
    data_reactive = r_qualitative_data
  ) # Modify later if it needs to return results
  
  # Clustering (takes ACP results)
  clustering_output_list <- mod_clustering_server(
    id = "clustering_module",
    acp_results_reactive = acp_results_reactive_from_module
  )
  
  # Regression (takes full data)
  mod_regression_server(
    id = "regression_module",
    data_reactive = r_full_data
  )
  
# ANOVA
mod_anova_server(
  id = "anova_module",
  data_reactive = r_full_data,
  clustered_data_reactive = reactive({
    # Safe check for clustering data
    clustering_output_list$data_clust() %||% NULL
  })
)
  
  # Chi-Squared (takes full data)
  mod_chisq_server(
    id = "chisq_module",
    data_reactive = r_full_data
  )
  
  # *** ADDED BACK call to Validation module ***
  mod_validation_server(
    id = "validation_module"
  )
  # *** End ADDED BACK call ***
  
} # End server function