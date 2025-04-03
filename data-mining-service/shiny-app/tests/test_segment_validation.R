# test_segment_validation.R
library(testthat)
library(dplyr)
library(ggplot2)

# Charger la fonction à tester
source("../scripts/segment_validation.R")

test_that("run_segment_validation fonctionne correctement", {
  # Créer un jeu de données de test
  transactions_sample <- data.frame(
    montant_total = rnorm(100),
    quantite = rnorm(100),
    prix_unitaire = rnorm(100),
    age = rnorm(100),
    temps_livraison_jours = rnorm(100),
    var_qualitative = sample(c("A", "B", "C"), 100, replace = TRUE)
  )
  
  # Appeler la fonction
  results <- run_segment_validation(transactions_sample, "var_qualitative")
  
  # Vérifier les sorties
  expect_true(is.table(results$cluster_table))
  expect_true(inherits(results$chi2_test, "htest"))
  expect_true(is.function(results$barplot_func))
  expect_true(is.function(results$mosaic_func))
  
  # Tester les graphiques
  barplot <- results$barplot_func()
  expect_true(inherits(barplot, "ggplot"))
  
  # Vérifier le cas d'erreur
  small_sample <- transactions_sample[1:2, ]
  error_results <- run_segment_validation(small_sample, "var_qualitative")
  expect_equal(error_results$error, "Pas assez de données pour le clustering")
})