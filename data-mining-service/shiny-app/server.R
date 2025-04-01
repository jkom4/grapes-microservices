# server.R
library(shiny)        # For creating the Shiny app
library(shinythemes)  # For applying themes to the Shiny app
library(dplyr)        # For data manipulation
library(ggplot2)      # For creating plots
library(mongolite)   # For connecting to MongoDB

server <- function(input, output) {
  # Load scripts
  source("scripts/segment_validation.R")
  source("scripts/acp.R")
  source("scripts/clustering.R")

  # Reactive variable to store data
  data_sample <- reactiveVal()

  # Function to load data (example with MongoDB)
  load_data <- function() {
    # Connect to MongoDB (adjust according to your parameters)
    transactions_collection <- mongo(collection = "transactions", db = "transactions_db", url = "mongodb://localhost:27017/")
    transactions_df <- transactions_collection$find()
    set.seed(123)
    sample_size <- min(1000, nrow(transactions_df))
    transactions_sample <- transactions_df %>% sample_n(sample_size)
    data_sample(transactions_sample)
    return(transactions_sample)
  }

  # Manage the "Validation des Segments Clients" tab
  observeEvent(input$run_analysis, {
    # Load data
    transactions_sample <- load_data()
    var_qualitative <- input$var_qualitative

    # Call the validation function
    results <- run_segment_validation(transactions_sample, var_qualitative)

    # Check for errors
    if (!is.null(results$error)) {
      showNotification(results$error, type = "error")
      return()
    }

    # Render results
    output$cluster_table <- renderTable({
      results$cluster_table
    })

    output$chi2_result <- renderPrint({
      results$chi2_test
    })

    output$barplot_clusters <- renderPlot({
      results$barplot_func()
    })

    output$mosaic_clusters <- renderPlot({
      results$mosaic_func()
    })
  })

  # Manage regression
  observeEvent(input$run_regression, {
    transactions_sample <- load_data()

    # Transform data
    transformed_data <- transform_data(transactions_sample)

    # Simple model example (adjust according to your needs)
    model <- lm(log_quantite ~ log_prix_unitaire + quantite_std, data = transformed_data)

    # Render results
    output$model_summary <- renderPrint({
      summary(model)
    })

    output$residuals_plot <- renderPlot({
      plot(model, which = 1)  # Residuals plot
      mtext("Residuals of the model", side = 3)
    })
  })

  # Manage PCA
  observeEvent(input$run_acp, {
    transactions_sample <- load_data()
    run_acp(transactions_sample, output)
  })

  # Manage clustering
  observeEvent(input$run_clustering, {
    transactions_sample <- load_data()
    results <- perform_clustering(transactions_sample)

    # Render results
    output$clustering_plot_12 <- renderPlot({
      print(results$clustering_plot_12)
    })

    output$clustering_plot_13 <- renderPlot({
      print(results$clustering_plot_13)
    })

    output$clustering_plot_14 <- renderPlot({
      print(results$clustering_plot_14)
    })

    output$clustering_summary <- renderPrint({
      results$clustering_summary
    })
  })
}
