library(testthat)
library(dplyr)
library(FactoMineR)
library(factoextra)

source("../scripts/acp.R")

# Write the test
test_that("run_acp fonctionne correctement", {
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
  results <- perform_acp(transactions_sample)
  
  # Check that the PCA object is correct
  expect_s3_class(results$acp_res, "PCA")
  
  # Check that the plots are ggplot objects
  expect_true(inherits(results$pca_eig_plot, "ggplot"))
  expect_true(inherits(results$pca_corr_plot_12, "ggplot"))
  expect_true(inherits(results$pca_corr_plot_13, "ggplot"))
  expect_true(inherits(results$pca_corr_plot_14, "ggplot"))
  expect_true(inherits(results$pca_plot_12, "ggplot"))
  expect_true(inherits(results$pca_plot_13, "ggplot"))
  expect_true(inherits(results$pca_plot_14, "ggplot"))
})
