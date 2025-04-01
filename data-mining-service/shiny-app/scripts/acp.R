# acp.R

library(FactoMineR)  # For performing PCA
library(factoextra) # For visualizing PCA results
library(dplyr)      # For data manipulation

# Function to perform PCA
perform_acp <- function(data) {
  # Select variables for PCA
  acp_data <- data %>%
    select(montant_total, quantite, prix_unitaire, age, temps_livraison_jours) %>%
    drop_na()

  # Perform PCA
  acp_res <- PCA(acp_data, scale.unit = TRUE, graph = FALSE)

  # Generate plots
  pca_eig_plot <- fviz_eig(acp_res, choice = "variance", geom = "bar")
  pca_corr_plot_12 <- fviz_pca_var(acp_res, axes = c(1, 2), col.var = "contrib", gradient.cols = c("blue", "red"))
  pca_corr_plot_13 <- fviz_pca_var(acp_res, axes = c(1, 3), col.var = "contrib", gradient.cols = c("blue", "red"))
  pca_corr_plot_14 <- fviz_pca_var(acp_res, axes = c(1, 4), col.var = "contrib", gradient.cols = c("blue", "red"))
  pca_plot_12 <- fviz_pca_ind(acp_res, axes = c(1, 2), col.ind = "cos2", gradient.cols = c("blue", "red"))
  pca_plot_13 <- fviz_pca_ind(acp_res, axes = c(1, 3), col.ind = "cos2", gradient.cols = c("blue", "red"))
  pca_plot_14 <- fviz_pca_ind(acp_res, axes = c(1, 4), col.ind = "cos2", gradient.cols = c("blue", "red"))

  return(list(
    acp_res = acp_res,
    pca_eig_plot = pca_eig_plot,
    pca_corr_plot_12 = pca_corr_plot_12,
    pca_corr_plot_13 = pca_corr_plot_13,
    pca_corr_plot_14 = pca_corr_plot_14,
    pca_plot_12 = pca_plot_12,
    pca_plot_13 = pca_plot_13,
    pca_plot_14 = pca_plot_14
  ))
}

# Main function for Shiny interface
run_acp <- function(data, output) {
  # Perform PCA
  results <- perform_acp(data)

  # Render plots in Shiny interface
  output$pca_eig <- renderPlot({
    print(results$pca_eig_plot)
  })

  output$pca_corr_12 <- renderPlot({
    print(results$pca_corr_plot_12)
  })

  output$pca_corr_13 <- renderPlot({
    print(results$pca_corr_plot_13)
  })

  output$pca_corr_14 <- renderPlot({
    print(results$pca_corr_plot_14)
  })

  output$pca_plot_12 <- renderPlot({
    print(results$pca_plot_12)
  })

  output$pca_plot_13 <- renderPlot({
    print(results$pca_plot_13)
  })

  output$pca_plot_14 <- renderPlot({
    print(results$pca_plot_14)
  })

  output$acp_summary <- renderPrint({
    summary(results$acp_res)
  })

  return(results$acp_res)
}
