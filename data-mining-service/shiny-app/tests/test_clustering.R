library(testthat)
library(dplyr)
library(ggplot2)
library(factoextra)
library(tidyr)

source("../scripts/clustering.R")

# Write the test
test_that("run_clustering fonctionne correctement", {
  # Create a sample dataset
  set.seed(123)
  transactions_sample <- data.frame(
    montant_total = rnorm(100),
    quantite = rnorm(100),
    prix_unitaire = rnorm(100),
    age = rnorm(100),
    temps_livraison_jours = rnorm(100)
  )
  
  # Run the function
  results <- perform_clustering(transactions_sample)
  
  # Check that the plots are ggplot objects
  expect_true(inherits(results$clustering_plot_12, "ggplot"))
  expect_true(inherits(results$clustering_plot_13, "ggplot"))
  expect_true(inherits(results$clustering_plot_14, "ggplot"))
  
  # Check the clustering summary
  expect_true(is.table(results$clustering_summary))
})
