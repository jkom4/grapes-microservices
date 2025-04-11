# modules/mod_statistiques.R

mod_statistiques_ui <- function(id) {
  ns <- NS(id)
  tagList(
    fluidRow(
      column(6,
             selectInput(ns("anova_var"), "Variable catégorielle:", choices = NULL),
             selectInput(ns("anova_dep"), "Variable dépendante:", choices = NULL),
             actionButton(ns("run_anova"), "Lancer ANOVA")
      ),
      column(6,
             verbatimTextOutput(ns("anova_result"))
      )
    )
  )
}

mod_statistiques_server <- function(id, data) {
  moduleServer(id, function(input, output, session) {
    observe({
      num_vars <- names(Filter(is.numeric, data))
      cat_vars <- names(Filter(is.factor, data))
      updateSelectInput(session, "anova_var", choices = cat_vars)
      updateSelectInput(session, "anova_dep", choices = num_vars)
    })
    
    observeEvent(input$run_anova, {
      req(input$anova_var, input$anova_dep)
      formula <- as.formula(paste(input$anova_dep, "~", input$anova_var))
      fit <- aov(formula, data = data)
      output$anova_result <- renderPrint({
        summary(fit)
      })
    })
  })
}
