# test_generate_data.R

library(testthat)

# Set the working directory to the root of your project
setwd("C:/Users/daive/Desktop/Q2 Masi/Projet_integre/projet-kube/grapes-microservices")

# Source the data generation script using a relative path
source("data-mining-service/scripts-r/genrate_data/generate_transaction_data.R")

test_that("Clients data frame is generated correctly", {
  expect_true(is.data.frame(clients))
  expect_true(nrow(clients) == total_clients)
  expect_true(all(c("client_id", "age", "region", "prefere_produit_local") %in% colnames(clients)))
})

test_that("Products data frame is generated correctly", {
  expect_true(is.data.frame(produits))
  expect_true(nrow(produits) == total_produits)
  expect_true(all(c("produit_id", "nom", "saison", "local", "prix_unitaire") %in% colnames(produits)))
})

test_that("Transactions data frame is generated correctly", {
  expect_true(is.data.frame(transactions))
  expect_true(nrow(transactions) == total_transactions)
  expect_true(all(c("transaction_id", "client_id", "produit_id", "quantite", "date_achat", "paiement_valide", "temps_livraison_jours", "montant_total") %in% colnames(transactions)))
})

test_that("Transactions contain negative quantities and prices", {
  expect_true(any(transactions$quantite < 0))
  expect_true(any(produits$prix_unitaire < 0))
})

test_that("CSV files are generated", {
  expect_true(file.exists("data-mining-service/data/raw/clients.csv"))
  expect_true(file.exists("data-mining-service/data/raw/produits.csv"))
  expect_true(file.exists("data-mining-service/data/raw/transactions.csv"))
})
