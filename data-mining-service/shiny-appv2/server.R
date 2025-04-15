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

# --- Server Function Definition ---
server <- function(input, output, session) {
  
  # --- Chargement des données ---
  data_loaded <- reactiveVal(NULL)
  observeEvent(TRUE, { showModal(modalDialog("Chargement...", footer=NULL)); loaded_list <- load_and_prepare_data(); removeModal(); if (!is.null(loaded_list)) { data_loaded(loaded_list); showNotification("Données chargées.", type = "message", duration=5)} else { showNotification("Échec chargement.", type = "error", duration = NULL) } }, once = TRUE)
  
  # --- Réactivité des données ---
  r_full_data <- reactive({ req(data_loaded()); data_loaded()$full_sampled_data })
  r_quantitative_data <- reactive({ req(data_loaded()); data_loaded()$quantitative_data })
  r_qualitative_data <- reactive({ req(data_loaded()); data_loaded()$qualitative_data })
  r_auth_summary <- reactive({ req(data_loaded()); data_loaded()$auth_summary })
  r_service_summary <- reactive({ req(data_loaded()); data_loaded()$service_summary })
  
  # --- Appel des Modules Serveur ---
  
  # *** ADDED BACK call to mod_overview_server ***
  mod_overview_server(
    id = "overview_module",
    data_reactive = r_full_data,
    auth_summary_reactive = r_auth_summary,
    service_summary_reactive = r_service_summary
  )
  
  # Call ACP module AND capture its returned reactive
  acp_results_reactive_from_module <- mod_acp_server("acp_module", data_reactive = r_quantitative_data)
  
  # Call ACM module (needs to return results if clustering uses it)
  # mca_results_reactive_from_module <- mod_acm_server("acm_module", data_reactive = r_qualitative_data) # Modify ACM to return results if needed
  
  # Call Clustering module, PASSING the reactive from the ACP module
  # Ensure acp_results_reactive is passed correctly
  mod_clustering_server("clustering_module", acp_results_reactive = acp_results_reactive_from_module)
  
}