# modules/mod_dashboard.R

mod_dashboard_ui <- function(id) {
  ns <- NS(id)
  tagList(
    fluidRow(
      column(4, verbatimTextOutput(ns("n_rows"))),
      column(4, verbatimTextOutput(ns("n_cols"))),
      column(4, verbatimTextOutput(ns("summary")))
    )
  )
}

mod_dashboard_server <- function(id, data) {
  moduleServer(id, function(input, output, session) {
    output$n_rows <- renderText({
      paste("Nombre de lignes:", nrow(data))
    })
    
    output$n_cols <- renderText({
      paste("Nombre de colonnes:", ncol(data))
    })
    
    output$summary <- renderPrint({
      summary(data)
    })
  })
}

