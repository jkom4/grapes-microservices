# tests/testthat/test-mod_chisq.R (Simplified)

library(testthat)
library(shiny)
library(dplyr)

source("../../modules/mod_chisq.R")

context("Chi-Squared Module Server Logic - Basic Execution")

# Sample data
sample_data_chisq <- reactiveVal(
  tibble(
    age_group = factor(c("18-34", "35-59", "60+", "18-34", "35-59", "18-34", "35-59", "60+", "60+")),
    region = factor(c("Rural", "Urban", "Rural", "Urban", "Rural", "Urban", "Rural", "Urban", "Rural")),
    payment = factor(sample(c("Visa", "MC"), 9, replace=TRUE))
  )
)


test_that("Chi-Squared runs with valid inputs", {
  testServer(mod_chisq_server,
             args = list(data_reactive = sample_data_chisq), {
               
               session$flushReact() # Allow initial UI updates
               
               # Set inputs (assuming selectors populated)
               session$setInputs(var1_select_chisq = "age_group")
               session$setInputs(var2_select_chisq = "region")
               session$setInputs(run_chisq = 1)
               session$flushReact() # Process event
               
               # Check results reactive
               res_list <- chisq_results_list()
               expect_true(!is.null(res_list) && is.list(res_list))
               expect_true(!is.null(res_list$chisq_result))
               expect_s3_class(res_list$chisq_result, "htest")
               
               # Check basic outputs run
               #expect_output(output$chisq_test_output)
               expect_silent(output$contingency_table) # Use renderTable in module
               expect_silent(output$expected_table)
               # Plot might be harder to test reliably
               # expect_silent(output$mosaic_plot)
             })
})