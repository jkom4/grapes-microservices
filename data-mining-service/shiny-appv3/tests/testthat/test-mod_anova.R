# tests/testthat/test-mod_anova.R

library(testthat)
library(shiny)
library(shinytest2)
library(dplyr)
library(tidyr)  # <<--- AJOUT OBLIGATOIRE

source("../../modules/mod_anova.R") # Corrige le chemin si nécessaire

test_that("mod_anova_server runs correctly without clusters", {
  sample_data <- reactive({
    tibble(
      montant_total = c(100, 150, 120, 180, 130, 170, 140),
      groupe_age = factor(c("Jeune", "Adulte", "Sénior", "Adulte", "Jeune", "Sénior", "Jeune"))
    )
  })
  
  testServer(mod_anova_server,
             args = list(
               data_reactive = sample_data,
               clustered_data_reactive = reactive(NULL)
             ), {
               # Simuler la sélection
               session$setInputs(quant_var = "montant_total")
               session$setInputs(cat_var = "groupe_age")
               session$setInputs(run_tukey = FALSE)
               session$setInputs(run_anova = 1)
               
               # Attendre l'exécution
               session$flushReact()
               
               # Vérification
               expect_type(rv$results, "list")
               expect_true("model" %in% names(rv$results))
               expect_s3_class(rv$results$model, "aov")
               expect_true("summary" %in% names(rv$results))
               expect_equal(rv$results$var_q, "montant_total")
               expect_equal(rv$results$var_c, "groupe_age")
             }
  )
})
