# modules/mod_overview.R

library(shiny)
library(shinydashboard)
library(dplyr)
library(DT)
library(ggplot2)
# library(plotly) # No longer needed if using renderPlot
library(scales)
library(shinycssloaders)
library(RColorBrewer)

# --- UI Function for Overview Module ---
mod_overview_ui <- function(id) {
  ns <- NS(id)
  tagList(
    h2("Vue d'ensemble des données"),
    fluidRow(
      valueBoxOutput(outputId = ns("vb_total_revenue"), width = 4),
      valueBoxOutput(outputId = ns("vb_total_transactions"), width = 4),
      valueBoxOutput(outputId = ns("vb_unique_clients"), width = 4)
    ),
    fluidRow(
      box(
        title = "Méthodes d'Authentification Utilisées", status = "success", solidHeader = TRUE, width = 6,
        # Using renderPlot for the pie chart now
        withSpinner(plotOutput(outputId = ns("plot_pie_auth_methods")), type = 6, color = "#2ECC71")
      ),
      box(
        title = "Top Services Demandés", status = "danger", solidHeader = TRUE, width = 6,
        withSpinner(plotOutput(outputId = ns("plot_bar_service_requests")), type = 6, color = "#E74C3C")
      )
    ),
    fluidRow(
      box(
        title = "Distribution du Montant par Groupe d'Âge", status = "primary", solidHeader = TRUE, width = 6,
        # Using renderPlot
        withSpinner(plotOutput(outputId = ns("plot_boxplot_age_montant")), type = 6, color = "#8E44AD")
      ),
      box(
        title = "Montant Total par Saison de Produit", status = "info", solidHeader = TRUE, width = 6,
        # Using renderPlot
        withSpinner(plotOutput(outputId = ns("plot_barchart_saison_montant")), type = 6, color = "#3498DB")
      )
    ),
    fluidRow(
      box(
        title = "Montant Total par Région Client", status = "warning", solidHeader = TRUE, width = 6,
        # Using renderPlot
        withSpinner(plotOutput(outputId = ns("plot_barchart_region_montant")), type = 6, color = "#F39C12")
      ),
      box(
        title = "Résumé Statistique (Échantillon Transactions)", status = "success", solidHeader = TRUE, width = 6,
        verbatimTextOutput(ns("summary"))
      )
    ),
    fluidRow(
      box(
        title = "Aperçu des Données Brutes (Échantillon Transactions)", status = "primary", solidHeader = TRUE, width = 12,
        withSpinner(DT::dataTableOutput(ns("head_data")), type = 6, color = "#8E44AD")
      )
    )
  )
}


# --- Server Function for Overview Module ---
mod_overview_server <- function(id, data_reactive, auth_summary_reactive, service_summary_reactive) {
  moduleServer(id, function(input, output, session) {
    
    df_reactive <- data_reactive
    auth_summary <- auth_summary_reactive
    service_summary <- service_summary_reactive
    
    # --- Value Boxes ---
    output$vb_total_revenue <- renderValueBox({ df <- df_reactive(); req(df, df$total_amount); total_rev <- sum(df$total_amount, na.rm = TRUE); valueBox( value = scales::dollar(total_rev, prefix = "", suffix = " €", big.mark = " ", decimal.mark = ","), subtitle = "Chiffre d'Affaires Total (Échantillon)", icon = icon("euro-sign"), color = "purple" ) })
    output$vb_total_transactions <- renderValueBox({ df <- df_reactive(); req(df); n_trans <- nrow(df); valueBox( value = formatC(n_trans, format="d", big.mark=" "), subtitle = "Nombre de Transactions (Échantillon)", icon = icon("receipt"), color = "blue" ) })
    output$vb_unique_clients <- renderValueBox({ df <- df_reactive(); req(df, df$client_id); n_clients <- n_distinct(df$client_id, na.rm = TRUE); valueBox( value = formatC(n_clients, format="d", big.mark=" "), subtitle = "Nombre de Clients Uniques (Échantillon)", icon = icon("users"), color = "yellow" ) })
    
    # --- Plot: Auth Methods Pie Chart (using renderPlot) ---
    output$plot_pie_auth_methods <- renderPlot({
      summary_data <- auth_summary()
      req(summary_data, nrow(summary_data) > 0) # Guard against NULL or empty data
      
      # Basic ggplot pie chart
      summary_data <- summary_data %>%
        mutate(prop = count / sum(count),
               ypos = cumsum(prop) - 0.5*prop,
               label_text = paste0(authentication_method, "\n", scales::percent(prop)))
      
      ggplot(summary_data, aes(x="", y=prop, fill=authentication_method)) +
        geom_bar(stat="identity", width=1, color="white") +
        coord_polar("y", start=0) +
        theme_void() + # remove background, grid, numeric labels
        # geom_text(aes(y = ypos, label = label_text), color = "black", size=3.5) + # Add labels
        scale_fill_brewer(palette="Set2") + # Use RColorBrewer palette
        labs(fill="Méthode", title=NULL) + # Remove default title, set legend title
        theme(legend.position = "right")
      
    })
    
    # --- Plot: Service Requests Bar Chart (using renderPlot) ---
    output$plot_bar_service_requests <- renderPlot({
      summary_data <- service_summary()
      req(summary_data, nrow(summary_data) > 0) # Guard
      
      ggplot(summary_data, aes(x = reorder(service_name, count), y = count, fill=service_name)) +
        geom_col() +
        coord_flip() +
        labs(title = NULL, x = "Service", y = "Nombre de Demandes") +
        theme_minimal() +
        theme(legend.position = "none")
      
    })
    
    # --- Plot: Age/Amount Boxplot (using renderPlot) ---
    output$plot_boxplot_age_montant <- renderPlot({
      df <- df_reactive()
      req(df, "age_group" %in% colnames(df), "total_amount" %in% colnames(df))
      df_plot <- df %>% filter(!is.na(age_group), !is.na(total_amount))
      req(nrow(df_plot) > 0) # Check rows AFTER filtering, before plotting
      # validate(need(nrow(df_plot) > 0, "...")) # Removed validate
      df_plot <- df_plot %>% mutate(age_group = factor(age_group, levels = c("0-17", "18-34", "35-59", "60+", "Unknown")))
      ggplot(df_plot, aes(x = age_group, y = total_amount, fill = age_group)) +
        geom_boxplot(na.rm = TRUE) +
        scale_y_continuous(labels = scales::comma_format(big.mark = " ", decimal.mark = ",")) +
        labs(title = NULL, x = "Groupe d'Âge", y = "Montant de la Transaction (€)") +
        theme_minimal() + theme(legend.position = "none")
    })
    
    # --- Plot: Saison/Amount Bar Chart (using renderPlot) ---
    output$plot_barchart_saison_montant <- renderPlot({
      df <- df_reactive()
      req(df, "saison" %in% colnames(df), "total_amount" %in% colnames(df))
      df_summary <- df %>% filter(!is.na(saison), saison != "Unknown", saison != "", !is.na(total_amount)) %>% group_by(saison) %>% summarise(montant_agg = sum(total_amount, na.rm = TRUE), .groups = 'drop') %>% filter(montant_agg > 0)
      req(nrow(df_summary) > 0) # Check summary has rows
      # validate(need(nrow(df_summary) > 0, "...")) # Removed validate
      ggplot(df_summary, aes(x = reorder(saison, -montant_agg), y = montant_agg, fill = saison)) + # Removed text aes
        geom_col(na.rm = TRUE) +
        scale_y_continuous(labels = scales::comma_format(big.mark = " ", decimal.mark = ",")) +
        labs(title = NULL, x = "Saison du Produit", y = "Montant Total (€)") +
        theme_minimal() + theme(legend.position = "none", axis.text.x = element_text(angle = 45, hjust = 1))
    })
    
    # --- Plot: Region/Amount Bar Chart (using renderPlot) ---
    output$plot_barchart_region_montant <- renderPlot({
      df <- df_reactive()
      req(df, "region" %in% colnames(df), "total_amount" %in% colnames(df))
      df_summary <- df %>% filter(!is.na(region), region != "Unknown", region != "", !is.na(total_amount)) %>% group_by(region) %>% summarise(montant_agg = sum(total_amount, na.rm = TRUE), .groups = 'drop') %>% filter(montant_agg > 0)
      req(nrow(df_summary) > 0) # Check summary has rows
      # validate(need(nrow(df_summary) > 0, "...")) # Removed validate
      ggplot(df_summary, aes(x = reorder(region, -montant_agg), y = montant_agg, fill = region)) + # Removed text aes
        geom_col(na.rm = TRUE) +
        scale_y_continuous(labels = scales::comma_format(big.mark = " ", decimal.mark = ",")) +
        labs(title = NULL, x = "Région du Client", y = "Montant Total (€)") +
        theme_minimal() + theme(legend.position = "none", axis.text.x = element_text(angle = 45, hjust = 1))
    })
    
    # --- Summary Output ---
    output$summary <- renderPrint({ df <- df_reactive(); req(df); summary(df) })
    # --- Head Data Table ---
    output$head_data <- DT::renderDataTable({ df <- df_reactive(); req(df); DT::datatable( df, options = list(scrollX = TRUE, pageLength = 5, lengthMenu = c(5, 10, 25, 50)), rownames = FALSE, filter = 'top' ) })
    
  }) # End moduleServer
}