# generate_data.R

# Load necessary libraries
library(dplyr)
library(lubridate)
library(ggplot2)
library(tidyr)
library(stringi)

# Define parameters
total_clients <- 2000
total_transactions <- 10000
total_produits <- 50
total_regions <- c("Urban - High Density", "Urban - Medium Density", "Rural")

set.seed(123)

# List of realistic fruit names
noms_fruits <- c("Apple", "Banana", "Orange", "Strawberry", "Mango", "Pear", "Grape", "Kiwi", "Cherry", "Watermelon",
                 "Melon", "Pineapple", "Lemon", "Raspberry", "Blueberry", "Peach", "Plum", "Pomegranate", "Lychee", "Apricot",
                 "Tomato", "Cucumber", "Carrot", "Beetroot", "Avocado", "Bell Pepper", "Onion", "Garlic", "Ginger", "Radish",
                 "Zucchini", "Sweet Potato", "Cabbage", "Spinach", "Lettuce", "Green Bean", "Pea", "Broccoli", "Turnip", "Fennel",
                 "Celery", "Cauliflower", "Corn", "Endive", "Asparagus", "Fig", "Walnut", "Almond", "Hazelnut", "Chestnut")

# Create directory if it doesn't exist
raw_data_dir <- "data-mining-service/data/raw"
if (!dir.exists(raw_data_dir)) {
  dir.create(raw_data_dir, recursive = TRUE)
}

# Generate clients with inconsistencies
clients <- data.frame(
  client_id = 1:total_clients,
  age = sample(c(5, 120, 18:80), total_clients, replace = TRUE),
  region = sample(c(total_regions, "Unknown", ""), total_clients, replace = TRUE, prob = c(0.5, 0.3, 0.15, 0.03, 0.02)),
  prefere_produit_local = sample(c(TRUE, FALSE, NA), total_clients, replace = TRUE, prob = c(0.55, 0.4, 0.05))
)

# Generate products with anomalies
produits <- data.frame(
  produit_id = 1:total_produits,
  nom = sample(noms_fruits, total_produits, replace = TRUE),
  saison = sample(c("Summer", "Winter", "All Seasons", "Unknown"), total_produits, replace = TRUE),
  local = sample(c(TRUE, FALSE, NA), total_produits, replace = TRUE, prob = c(0.7, 0.25, 0.05)),
  prix_unitaire = round(runif(total_produits, -5, 15), 2)
)

# Generate transactions with errors
transactions <- data.frame(
  transaction_id = 1:total_transactions,
  client_id = sample(c(clients$client_id, 9999), total_transactions, replace = TRUE),
  produit_id = sample(produits$produit_id, total_transactions, replace = TRUE),
  quantite = sample(c(1:10, -3, 0), total_transactions, replace = TRUE),
  date_achat = sample(c(seq(as.Date('2023-01-01'), as.Date('2024-01-01'), by="day"), NA), total_transactions, replace = TRUE),
  paiement_valide = sample(c(TRUE, FALSE, NA), total_transactions, replace = TRUE, prob = c(0.94, 0.05, 0.01)),
  temps_livraison_jours = sample(c(1:7, -2, 50), total_transactions, replace = TRUE)
)

# Join with products to get the total amount
transactions <- transactions %>%
  left_join(produits, by = "produit_id") %>%
  mutate(montant_total = quantite * prix_unitaire)

# Add client information to transactions
transactions <- transactions %>%
  left_join(clients, by = "client_id")

# Export CSV files with debug statements
write.csv(clients, file.path(raw_data_dir, "clients.csv"), row.names = FALSE)
write.csv(produits, file.path(raw_data_dir, "produits.csv"), row.names = FALSE)
write.csv(transactions, file.path(raw_data_dir, "transactions.csv"), row.names = FALSE)

# Print debug statements
print(file.exists(file.path(raw_data_dir, "clients.csv")))
print(file.exists(file.path(raw_data_dir, "produits.csv")))
print(file.exists(file.path(raw_data_dir, "transactions.csv")))

# Visualization of transaction distribution
ggplot(transactions, aes(x = montant_total)) +
  geom_histogram(fill = "blue", bins = 30) +
  ggtitle("Distribution of Transaction Amounts with Raw Data")

