# tests/testthat/test-mod_acp.R

library(testthat)
library(shiny)
library(dplyr)
library(FactoMineR)
library(ggplot2)
library(tidyr)    # <<< ADD Library >>>

source("../../modules/mod_acp.R") # Adjust path if needed

context("ACP Module Server Logic")

test_that("ACP module calculates results correctly with valid inputs", {
  
  # Create sample quantitative data
  sample_data <- reactiveVal(
    data.frame(
      age = c(25, 45, 60, 33, 50), # Removed rows with NA for direct test use
      quantity = c(5, 2, 1, 10, 4),
      unit_price = c(10.5, 22.1, 15.0, 5.2, 18.0),
      total_amount = c(52.5, 44.2, 15.0, 52.0, 72.0),
      delivery_time_days = c(2, 4, 1, 3, 2) # Filled NAs
    ) # No need for drop_na here if source is clean
  )
  
  expected_vars <- c("age", "quantity", "unit_price", "total_amount", "delivery_time_days")
  
  testServer(mod_acp_server, args = list(data_reactive = sample_data), {
    session$setInputs(vars_select = expected_vars)
    session$setInputs(axe_x = 1); session$setInputs(axe_y = 2)
    session$setInputs(run_pca = 1)
    
    # Flush reactives to ensure eventReactive runs
    session$flushReact()
    
    pca_res <- acp_results()
    expect_false(is.null(pca_res), "PCA results should not be NULL.")
    
    # <<< FIX: Remove the third argument STRING >>>
    expect_s3_class(pca_res, "PCA") # NOW Correct
    
    # Check downstream reactives
    expect_s3_class(eig_plot_obj(), "ggplot")
    expect_s3_class(ind_plot_obj(), "ggplot")
    expect_s3_class(var_plot_obj(), "ggplot")
    expect_s3_class(contrib_plot_x_obj(), "ggplot")
    expect_s3_class(contrib_plot_y_obj(), "ggplot")
    
    contrib_df <- contrib_data_reactive()
    cos2_df <- cos2_data_reactive()
    expect_s3_class(contrib_df, "data.frame")
    expect_s3_class(cos2_df, "data.frame")
    n_vars <- length(expected_vars)
    expect_equal(nrow(contrib_df), n_vars, label = "Contrib table rows.")
    expect_equal(nrow(cos2_df), n_vars, label = "Cos2 table rows.")
    
    axes <- selected_axes()
    expect_equal(axes, c(1, 2))
    
    expect_silent(output$head_data_pca)
  })
})

test_that("ACP module handles NULL input data gracefully", {
  null_data <- reactiveVal(NULL)
  testServer(mod_acp_server, args = list(data_reactive = null_data), {
    session$setInputs(vars_select = c("a", "b")); session$setInputs(axe_x = 1); session$setInputs(axe_y = 2)
    session$setInputs(run_pca = 1)
    session$flushReact()
    
    # <<< FIX: Check specifically for errors or expect NULL >>>
    # Option 1: Expect NULL directly (might still give silent error if req fails deeply)
    # expect_null(acp_results(), "PCA results should be NULL with NULL input.")
    
    # Option 2: Check if the reactive errors out (might be more robust)
    expect_error(acp_results()) # Expect an error/stop due to req() failure
    
    # Option 3: Simplest check - just run and ensure testServer doesn't crash
    # (Do nothing specific here, test pass means no fatal error)
    
  })
})