library(shiny)

# load server and ui components
source("ui.R")
source("server.R")

# Run the app
shinyApp(ui = ui, server = server)