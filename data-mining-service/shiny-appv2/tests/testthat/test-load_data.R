# tests/testthat/test-load_data.R
library(testthat)
test_that("Données correctement chargées", {
  data <- load_and_prepare_data()
  expect_true("quantitative_data" %in% names(data))
  expect_true(nrow(data$quantitative_data) > 0)
  expect_true("qualitative_data" %in% names(data))
})