# modules/mod_regression.R
mod_regression_ui <- function(id) {
  ns <- NS(id)
  fluidPage(
    h3("Régressions"),
    selectInput(ns("y"), "Variable à prédire", choices = NULL),
    selectInput(ns("x"), "Prédicteurs", choices = NULL, multiple = TRUE),
    verbatimTextOutput(ns("model"))
  )
}

mod_regression_server <- function(id, data) {
  moduleServer(id, function(input, output, session) {
    updateSelectInput(session, "y", choices = colnames(data$quantitative_data))
    updateSelectInput(session, "x", choices = colnames(data$quantitative_data))
    output$model <- renderPrint({
      req(input$x, input$y)
      form <- as.formula(paste(input$y, "~", paste(input$x, collapse = "+")))
      summary(lm(form, data = data$quantitative_data))
    })
  })
}
