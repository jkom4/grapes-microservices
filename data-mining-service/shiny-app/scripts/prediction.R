predict_sales <- function(data, input, output) {
  model <- lm(montant_total ~ quantite, data = data)
  
  new_data <- data.frame(quantite = as.numeric(input$new_freq))
  prediction <- predict(model, new_data)
  
  output$prediction_result <- renderText({
    paste("Prédiction du montant d'achat : ", round(prediction, 2), "€")
  })
}
