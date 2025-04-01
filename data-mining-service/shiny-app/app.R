library(shiny)

# Charger les composants de l'UI et du serveur
source("ui.R")
source("server.R")

# Lancer l'application
shinyApp(ui = ui, server = server)
