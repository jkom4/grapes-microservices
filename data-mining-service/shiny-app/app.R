
# The main purpose of this file,it's to start our shiny app by load the main files and run the app

library(shiny)

# load all components (UI and SERVER ) for shiny App
source("ui.R")
source("server.R")

# run the app
shinyApp(ui = ui, server = server)
