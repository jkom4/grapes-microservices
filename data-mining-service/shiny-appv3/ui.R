# ui.R

# --- Libraries for UI ---
library(shiny)
library(shinydashboard)
library(plotly)
library(DT)
library(shinycssloaders)

# --- Source Modules ---
source("modules/mod_overview.R")
source("modules/mod_acp.R")
source("modules/mod_acm.R")
source("modules/mod_clustering.R")
source("modules/mod_regression.R")
source("modules/mod_anova.R")
source("modules/mod_chisq.R")
source("modules/mod_validation.R") # Ensure validation module is sourced

# --- UI Definition ---
ui <- dashboardPage(
  skin = "purple",
  dashboardHeader(title = "Grapes Insights", titleWidth = 250),
  dashboardSidebar(
    width = 250,
    sidebarMenu(
      id = "tabs",
      menuItem(text = "Vue d'ensemble", tabName = "overview", icon = icon("tachometer-alt")),
      menuItem(text = "Analyses Factorielles", icon = icon("project-diagram"), startExpanded = FALSE,
               menuSubItem(text = "ACP (Quantitatif)", tabName = "acp", icon = icon("chart-line")),
               menuSubItem(text = "ACM (Qualitatif)", tabName = "acm", icon = icon("chart-pie"))
      ),
      menuItem(text = "Clustering (CAH)", tabName = "clustering", icon = icon("users-cog")),
      menuItem(text = "Régression", tabName = "regression", icon = icon("chart-area")),
      menuItem(text = "Tests Statistiques", icon = icon("flask"), startExpanded = FALSE, # Combined Tests
               menuSubItem(text = "ANOVA", tabName = "anova", icon = icon("chart-bar")),
               menuSubItem(text = "Chi-Carré", tabName = "chisq", icon = icon("table-list"))
      ),
      # <<< VALIDATION MENU ITEM >>>
      menuItem(text = "Validation (CDS)", tabName = "validation", icon = icon("check-circle"))
    ) # End sidebarMenu
  ), # End dashboardSidebar
  
  dashboardBody(
    tags$head( tags$style(HTML(".shiny-spinner-output-container { position: relative; } .load-container { position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); }")) ),
    tabItems(
      tabItem(tabName = "overview", mod_overview_ui("overview_module"), hr(), h4("DEBUG"), verbatimTextOutput("debug_qldata")), # Debug here
      tabItem(tabName = "acp", mod_acp_ui("acp_module")),
      tabItem(tabName = "acm", mod_acm_ui("acm_module")),
      tabItem(tabName = "clustering", mod_clustering_ui("clustering_module")),
      tabItem(tabName = "regression", mod_regression_ui("regression_module")),
      tabItem(tabName = "anova", mod_anova_ui("anova_module")),
      tabItem(tabName = "chisq", mod_chisq_ui("chisq_module")),
      # <<< ADD VALIDATION TAB ITEM >>>
      tabItem(tabName = "validation", mod_validation_ui("validation_module"))
      # <<< END ADD >>>
    ) # End tabItems
  ) # End dashboardBody
) # End dashboardPage