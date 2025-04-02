# entrypoint.R

# Load necessary libraries
library(dplyr)
library(ggplot2)
library(lubridate)
library(tidyr)
library(stringi)
library(mongolite)

# Source the data generation script
source("scripts-r/generate_data/generate_transaction_data.R")

# Connect to MongoDB with authentication
cat("Connecting to MongoDB...\n")
mongo <- mongo(collection = "transactions", db = "OperationsDB", url = "mongodb://root:SparringMASI!@localhost:27017/?authSource=admin")
cat("Connected to MongoDB\n")

# Insert data into MongoDB collections
mongo$insert(clients)
cat("Clients data inserted into MongoDB\n")
mongo$insert(produits)
cat("Produits data inserted into MongoDB\n")
mongo$insert(transactions)
cat("Transactions data inserted into MongoDB\n")
