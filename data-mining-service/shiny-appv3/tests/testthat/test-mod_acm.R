# tests/testthat/test-mod_acm.R

library(testthat)
library(shiny)
library(dplyr)
library(FactoMineR)
library(ggplot2)

source("../../modules/mod_acm.R") # Adjust path if needed

context("ACM Module Server Logic")

test_that("ACM module calculates results correctly with valid inputs", {
  
  sample_data_quali <- reactiveVal(
    data.frame(
      region = factor(c("Rural", "Urban - High Density", "Rural", "Urban - Medium Density", "Unknown", "Rural")),
      age_group = factor(c("Adulte", "Jeune Adulte", "Sénior", "Adulte", "Adulte", "Jeune Adulte")),
      payment_method = factor(c("Visa", "Mastercard", "Visa", "Visa", "Mastercard", "Mastercard")),
      saison = factor(c("Summer", "Winter", "All Seasons", "Summer", "Winter", "Spring"))
    )
    # No need for mutate if already factors
  )
  
  expected_vars <- c("region", "age_group", "payment_method", "saison")
  
  testServer(mod_acm_server, args = list(data_reactive = sample_data_quali), {
    session$setInputs(vars_select_mca = expected_vars)
    session$setInputs(axe_x_mca = 1); session$setInputs(axe_y_mca = 2)
    session$setInputs(run_mca = 1)
    session$flushReact() # Ensure observeEvent triggers
    
    mca_res <- mca_results()
    
    # <<< FIX: Check for NULL before checking class >>>
    expect_false(is.null(mca_res), "MCA results reactive should not be NULL.")
    # If it's not NULL, then check the class
    if (!is.null(mca_res)) {
      expect_s3_class(mca_res, "MCA") # Removed 3rd arg
    }
    
    # Check derived reactives
    expect_s3_class(eig_plot_mca_obj(), "ggplot")
    expect_s3_class(biplot_mca_obj(), "ggplot")
    expect_s3_class(contrib_plot_mca_x_obj(), "ggplot")
    expect_s3_class(contrib_plot_mca_y_obj(), "ggplot")
    
    mca_var_res <- get_mca_var_results()
    expect_type(mca_var_res, "list")
    expect_true(all(c("coord", "contrib", "cos2") %in% names(mca_var_res)))
    expect_s3_class(contrib_data_mca_reactive(), "data.frame")
    expect_s3_class(cos2_data_mca_reactive(), "data.frame")
    
    axes <- selected_axes_mca()
    expect_equal(axes, c(1, 2))
    
    expect_silent(output$head_data_mca)
  })
})

test_that("ACM module handles NULL input data gracefully", {
  null_data <- reactiveVal(NULL)
  testServer(mod_acm_server, args = list(data_reactive = null_data), {
    session$setInputs(vars_select_mca = c("a", "b"))
    session$setInputs(axe_x_mca = 1); session$setInputs(axe_y_mca = 2)
    session$setInputs(run_mca = 1)
    session$flushReact()
    
    # <<< FIX: Check specifically for errors or expect NULL >>>
    # Option 1: Expect NULL directly
    # expect_null(mca_results(), "MCA results should be NULL.")
    
    # Option 2: Expect an error/stop due to req() failure
    expect_error(mca_results())
    
    # Test a downstream reactive that depends on the errored one
    expect_error(get_mca_var_results()) # This should also fail
    
  })
})