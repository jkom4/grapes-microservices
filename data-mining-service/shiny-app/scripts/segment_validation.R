# segment_validation.R

library(dplyr)  # For data manipulation
library(ggplot2) # For creating plots

# Function to validate customer segments
run_segment_validation <- function(transactions_sample, var_qualitative) {
  # Check if data and qualitative variable are valid
  if (is.null(transactions_sample) || nrow(transactions_sample) == 0) {
    return(list(error = "No data available"))
  }
  if (!var_qualitative %in% names(transactions_sample)) {
    return(list(error = "Invalid qualitative variable"))
  }

  # Prepare data for clustering
  acp_data <- transactions_sample %>%
    select(all_of(c("montant_total", "quantite", "prix_unitaire", "age", "temps_livraison_jours"))) %>%
    na.omit()

  # Check the number of rows
  if (nrow(acp_data) < 3) {
    return(list(error = "Not enough data for clustering"))
  }

  # Perform k-means clustering
  set.seed(123)
  km_res <- kmeans(scale(acp_data), centers = 3, nstart = 25)

  # Add clusters to the dataframe
  transactions_sample$cluster <- NA
  transactions_sample$cluster[complete.cases(transactions_sample[, c("montant_total", "quantite", "prix_unitaire", "age", "temps_livraison_jours")])] <- km_res$cluster
  transactions_sample$cluster <- as.factor(transactions_sample$cluster)

  # Create a cross table
  cluster_table <- table(transactions_sample$cluster, transactions_sample[[var_qualitative]])

  # Perform chi-square test
  chi2_test <- chisq.test(cluster_table)

  # Function to create a barplot
  barplot_func <- function() {
    ggplot(transactions_sample, aes(x = .data[[var_qualitative]], fill = cluster)) +
      geom_bar(position = "dodge") +
      ggtitle(paste("Distribution of clusters by", var_qualitative)) +
      theme_minimal()
  }

  # Function to create a mosaic plot
  mosaic_func <- function() {
    mosaicplot(cluster_table, main = paste("Mosaic of Clusters vs", var_qualitative), color = TRUE)
  }

  # Return a list with the results
  return(list(
    cluster_table = cluster_table,
    chi2_test = chi2_test,
    barplot_func = barplot_func,
    mosaic_func = mosaic_func
  ))
}
