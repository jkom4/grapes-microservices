# ui.R

# --- Libraries for UI ---
library(shiny)
library(shinydashboard)
library(plotly)      # For plotlyOutput
library(DT)          # For dataTableOutput
library(shinycssloaders) # For withSpinner

# --- Source Modules (for UI functions) ---
source("modules/mod_overview.R")
source("modules/mod_acp.R")
source("modules/mod_acm.R")
source("modules/mod_clustering.R")

# --- UI Definition ---
ui <- dashboardPage(
  skin = "purple",
  dashboardHeader(title = "Grapes Insights", titleWidth = 250),
  
  dashboardSidebar(
    width = 250,
    # Wrap the menuItems directly inside sidebarMenu
    sidebarMenu(
      id = "tabs", # ID for the sidebarMenu itself
      menuItem(text = "Vue d'ensemble", tabName = "overview", icon = icon("tachometer-alt")),
      menuItem(text = "Analyses Factorielles", icon = icon("project-diagram"), startExpanded = FALSE,
               # Sub-items go inside the menuItem that contains them
               menuSubItem(text = "ACP (Quantitatif)", tabName = "acp", icon = icon("chart-line")),
               menuSubItem(text = "ACM (Qualitatif)", tabName = "acm", icon = icon("chart-pie"))
      ), # End of "Analyses Factorielles" menuItem
      menuItem(text = "Clustering (CAH)", tabName = "clustering", icon = icon("users-cog"))
      # Add other top-level menuItems here if needed...
    ) # End sidebarMenu
  ), # End dashboardSidebar
  
  dashboardBody(
    tags$head(
      tags$style(HTML(".shiny-spinner-output-container { position: relative; } .load-container { position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); }"))
    ),
    tabItems(
      # --- Use Module UIs for each tab ---
      tabItem(tabName = "overview", mod_overview_ui("overview_module")),
      tabItem(tabName = "acp", mod_acp_ui("acp_module")),
      tabItem(tabName = "acm", mod_acm_ui("acm_module")), # REMOVED extra comma here
      tabItem(tabName = "clustering", mod_clustering_ui("clustering_module"))
    ) # End tabItems
  ) # End dashboardBody
) # End dashboardPage