library(shiny)        # For creating the Shiny app
library(shinythemes)  # For applying themes to the Shiny app

ui <- fluidPage(
  theme = shinytheme("cosmo"),  # Apply a Bootstrap theme
  titlePanel("Transaction Analysis Dashboard"),
  tags$head(
    tags$style(HTML("
      body {
        background-color: #f8f9fa;
        font-family: 'Arial', sans-serif;
      }
      .main-title {
        color: #28a745;
        font-size: 24px;
        font-weight: bold;
      }
      .nav-tabs {
        border-bottom: 2px solid #28a745;
      }
      .nav-tabs .nav-link.active {
        background-color: #28a745;
        color: white;
      }
      .card {
        border: 1px solid #ddd;
        border-radius: 8px;
        box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
        margin-bottom: 20px;
      }
      .card-header {
        background-color: #28a745;
        color: white;
        font-weight: bold;
      }
    "))
  ),
  tabsetPanel(
    tabPanel("PCA",
             sidebarLayout(
               sidebarPanel(
                 actionButton("run_acp", "Run PCA Analysis", class = "btn btn-success")
               ),
               mainPanel(
                 div(class = "card",
                     div(class = "card-header", "Eigenvalue Plot"),
                     plotOutput("pca_eig")
                 ),
                 div(class = "card",
                     div(class = "card-header", "Correlation Sphere Dim1 vs Dim2"),
                     plotOutput("pca_corr_12")
                 ),
                 div(class = "card",
                     div(class = "card-header", "Correlation Sphere Dim1 vs Dim3"),
                     plotOutput("pca_corr_13")
                 ),
                 div(class = "card",
                     div(class = "card-header", "Correlation Sphere Dim1 vs Dim4"),
                     plotOutput("pca_corr_14")
                 ),
                 div(class = "card",
                     div(class = "card-header", "Projection Dim1 vs Dim2"),
                     plotOutput("pca_plot_12")
                 ),
                 div(class = "card",
                     div(class = "card-header", "Projection Dim1 vs Dim3"),
                     plotOutput("pca_plot_13")
                 ),
                 div(class = "card",
                     div(class = "card-header", "Projection Dim1 vs Dim4"),
                     plotOutput("pca_plot_14")
                 ),
                 div(class = "card",
                     div(class = "card-header", "PCA Statistical Summary"),
                     verbatimTextOutput("acp_summary")
                 )
               )
             )
    ),
    tabPanel("Clustering",
             sidebarLayout(
               sidebarPanel(
                 actionButton("run_clustering", "Run Clustering", class = "btn btn-success")
               ),
               mainPanel(
                 div(class = "card",
                     div(class = "card-header", "Clustering Dim1 vs Dim2"),
                     plotOutput("clustering_plot_12")
                 ),
                 div(class = "card",
                     div(class = "card-header", "Clustering Dim1 vs Dim3"),
                     plotOutput("clustering_plot_13")
                 ),
                 div(class = "card",
                     div(class = "card-header", "Clustering Dim1 vs Dim4"),
                     plotOutput("clustering_plot_14")
                 ),
                 div(class = "card",
                     div(class = "card-header", "Clustering Summary"),
                     verbatimTextOutput("clustering_summary")
                 )
               )
             )
    ),
    tabPanel("Customer Segment Validation",
             sidebarLayout(
               sidebarPanel(
                 actionButton("run_analysis", "Run Analysis", class = "btn btn-success"),
                 selectInput("var_qualitative", "Qualitative Variable", choices = c("region", "season"))
               ),
               mainPanel(
                 div(class = "card",
                     div(class = "card-header", "Cluster Distribution"),
                     plotOutput("barplot_clusters")
                 ),
                 div(class = "card",
                     div(class = "card-header", "Cluster Mosaic"),
                     plotOutput("mosaic_clusters")
                 ),
                 div(class = "card",
                     div(class = "card-header", "Cluster Cross Table"),
                     tableOutput("cluster_table")
                 ),
                 div(class = "card",
                     div(class = "card-header", "Chi2 Test Result"),
                     verbatimTextOutput("chi2_result")
                 )
               )
             )
    ),
    tabPanel("Multiple Linear Regression",
             sidebarLayout(
               sidebarPanel(
                 actionButton("run_regression", "Run Regression", class = "btn btn-success")
               ),
               mainPanel(
                 div(class = "card",
                     div(class = "card-header", "Base Model Summary"),
                     verbatimTextOutput("model_base_summary")
                 ),
                 div(class = "card",
                     div(class = "card-header", "Residuals Histogram - Base Model"),
                     plotOutput("residuals_hist_base")
                 ),
                 div(class = "card",
                     div(class = "card-header", "QQ-Plot of Residuals - Base Model"),
                     plotOutput("residuals_qq_base")
                 ),
                 div(class = "card",
                     div(class = "card-header", "Homoscedasticity - Base Model"),
                     plotOutput("residuals_vs_fitted_base")
                 ),
                 div(class = "card",
                     div(class = "card-header", "Logarithmic Model Summary"),
                     verbatimTextOutput("model_log_summary")
                 ),
                 div(class = "card",
                     div(class = "card-header", "Polynomial Model Summary"),
                     verbatimTextOutput("model_poly_summary")
                 ),
                 div(class = "card",
                     div(class = "card-header", "Standardized Model Summary"),
                     verbatimTextOutput("model_std_summary")
                 ),
                 div(class = "card",
                     div(class = "card-header", "Residuals Analysis - Base Model"),
                     plotOutput("residuals_plot_base")
                 ),
                 div(class = "card",
                     div(class = "card-header", "Residuals Analysis - Logarithmic Model"),
                     plotOutput("residuals_plot_log")
                 ),
                 div(class = "card",
                     div(class = "card-header", "Residuals Analysis - Polynomial Model"),
                     plotOutput("residuals_plot_poly")
                 ),
                 div(class = "card",
                     div(class = "card-header", "Residuals Analysis - Standardized Model"),
                     plotOutput("residuals_plot_std")
                 )
               )
             )
    )
  )
)
