# modules/mod_exploration.R

mod_exploration_ui <- function(id) {
  ns <- NS(id)
  tagList(
    sidebarLayout(
      sidebarPanel(
        selectInput(ns("var"), "Variable à explorer:", choices = NULL)
      ),
      mainPanel(
        plotOutput(ns("hist_plot"))
      )
    )
  )
}

mod_exploration_server <- function(id, data) {
  moduleServer(id, function(input, output, session) {
    observe({
      updateSelectInput(session, "var", choices = names(data))
    })
    
    output$hist_plot <- renderPlot({
      req(input$var)
      hist(data[[input$var]], main = paste("Histogramme de", input$var), col = "purple", border = "white")
    })
  })
}