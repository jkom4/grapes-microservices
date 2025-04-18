# tests/testthat/test-mod_regression.R

library(testthat)
library(shiny)
library(dplyr)
library(broom) # For tidy()

# Source the module file
source("../../modules/mod_regression.R") # Adjust path if needed

context("Regression Module Server Logic")

# Prepare sample data (similar to r_full_data)
sample_data_reg <- reactiveVal(
  tibble(
    total_amount = c(52.5, 44.2, 15.0, 52.0, 72.0, 76.0, 95.0),
    age = c(25L, 45L, 60L, 33L, 50L, 28L, 41L),
    quantity = c(5, 2, 1, 10, 4, 8, 7),
    delivery_time_days = c(2L, 4L, 1L, 3L, 3L, 5L, 2L),
    region = factor(c("Rural", "Urban", "Rural", "Urban", "Rural", "Urban", "Rural")),
    prefere_produit_local = factor(c(1, 0, 1, 1, 0, 0, 1), levels=c(0,1)) # Example binary 0/1 as factor
  )
)

test_that("Linear Regression runs and produces output", {
  testServer(mod_regression_server, args = list(data_reactive = sample_data_reg), {
    # Set inputs for linear regression
    session$setInputs(model_type = "linear")
    # Wait for UI updates (dependent var choices might need data_reactive)
    session$flushReact()
    session$setInputs(vars_select_dependent = "total_amount")
    session$setInputs(vars_select_independent = c("age", "quantity"))
    session$setInputs(run_regression = 1) # Trigger calculation
    session$flushReact() # Ensure eventReactive runs
    
    # Check model output
    model_obj <- regression_model()
    expect_false(is.null(model_obj), "Linear model object should not be NULL.")
    expect_s3_class(model_obj, "lm")
    
    # Check summary output render
    expect_output(output$model_summary)
    
    # Check diagnostic plot render (just check it runs)
    expect_silent(output$diagnostic_plots)
    
    # Check data preview render
    expect_silent(output$head_data_regression)
  })
})

test_that("Logistic Regression runs and produces output", {
  testServer(mod_regression_server, args = list(data_reactive = sample_data_reg), {
    # Set inputs for logistic regression
    session$setInputs(model_type = "logistic")
    # Wait for UI updates (dependent var choices)
    session$flushReact()
    session$setInputs(vars_select_dependent = "prefere_produit_local")
    session$setInputs(vars_select_independent = c("age", "region"))
    session$setInputs(run_regression = 1)
    session$flushReact()
    
    # Check model output
    model_obj <- regression_model()
    expect_false(is.null(model_obj), "Logistic model object should not be NULL.")
    expect_s3_class(model_obj, "glm")
    expect_equal(model_obj$family$family, "binomial")
    
    # Check summary output render
    expect_output(output$model_summary)
    
    # Check logistic extras render
    expect_output(output$logistic_extras)
    
    # Check data preview render
    expect_silent(output$head_data_regression)
  })
})


test_that("Regression handles insufficient data", {
  # Provide data that will become insufficient after na.omit
  # only 2 rows, need 3 for Y ~ X1 + intercept
  insuff_data <- reactiveVal(
    tibble(
      total_amount = c(52.5, NA),
      age = c(25L, 45L),
      quantity = c(5, 2)
    )
  )
  testServer(mod_regression_server, args = list(data_reactive = insuff_data), {
    session$setInputs(model_type = "linear")
    session$flushReact()
    session$setInputs(vars_select_dependent = "total_amount")
    session$setInputs(vars_select_independent = c("age", "quantity"))
    # Run button - data_for_regression should validate & fail
    session$setInputs(run_regression = 1)
    session$flushReact()
    
    # Check that the reactive preparing data errors out or returns NULL via req(FALSE)
    expect_error(data_for_regression())
    # Check that model itself is not calculated
    expect_null(regression_model())
    
  })
})