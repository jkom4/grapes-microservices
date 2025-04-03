# clustering.R
# Purpose: Performs k-means clustering and generates the necessary plots.
# Imports: dplyr for data manipulation, ggplot2 for creating plots, factoextra for visualizing clustering results.
# Returns: A list containing the clustering plots and a summary table.

library(dplyr)      # For data manipulation
library(ggplot2)   # For creating plots
library(factoextra) # For visualizing clustering results

# Function to perform clustering
perform_clustering <- function(data) {
  # Select variables for clustering
  clustering_data <- data %>%
    select(montant_total, quantite, prix_unitaire, age, temps_livraison_jours) %>%
    drop_na()

  # Perform k-means clustering
  kmeans_res <- kmeans(scale(clustering_data), centers = 3, nstart = 25)

  # Add clusters to the dataframe
  data$cluster <- as.factor(kmeans_res$cluster)

  # Generate clustering plots
  clustering_plot_12 <- fviz_cluster(list(data = clustering_data, cluster = kmeans_res$cluster), axes = c(1, 2)) +
    ggtitle("Clustering Dim1 vs Dim2")

  clustering_plot_13 <- fviz_cluster(list(data = clustering_data, cluster = kmeans_res$cluster), axes = c(1, 3)) +
    ggtitle("Clustering Dim1 vs Dim3")

  clustering_plot_14 <- fviz_cluster(list(data = clustering_data, cluster = kmeans_res$cluster), axes = c(1, 4)) +
    ggtitle("Clustering Dim1 vs Dim4")

  return(list(
    clustering_plot_12 = clustering_plot_12,
    clustering_plot_13 = clustering_plot_13,
    clustering_plot_14 = clustering_plot_14,
    clustering_summary = table(data$cluster)
  ))
}
