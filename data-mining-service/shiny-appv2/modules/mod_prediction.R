# modules/mod_prediction.R

mod_prediction_ui <- function(id) {
  ns <- NS(id)
  tagList(
    fluidRow(
      column(4,
             selectInput(ns("x_var"), "Variable explicative:", choices = NULL),
             selectInput(ns("y_var"), "Variable à prédire:", choices = NULL),
             actionButton(ns("run_lm"), "Lancer Régression")
      ),
      column(8,
             verbatimTextOutput(ns("lm_summary")),
             plotOutput(ns("lm_plot"))
      )
    )
  )
}

mod_prediction_server <- function(id, data) {
  moduleServer(id, function(input, output, session) {
    observe({
      num_vars <- names(Filter(is.numeric, data))
      updateSelectInput(session, "x_var", choices = num_vars)
      updateSelectInput(session, "y_var", choices = num_vars)
    })
    
    observeEvent(input$run_lm, {
      req(input$x_var, input$y_var)
      model <- lm(data[[input$y_var]] ~ data[[input$x_var]])
      output$lm_summary <- renderPrint({
        summary(model)
      })
      output$lm_plot <- renderPlot({
        plot(data[[input$x_var]], data[[input$y_var]],
             main = "Régression linéaire",
             xlab = input$x_var,
             ylab = input$y_var,
             pch = 19, col = "darkgreen")
        abline(model, col = "red", lwd = 2)
      })
    })
  })
}
