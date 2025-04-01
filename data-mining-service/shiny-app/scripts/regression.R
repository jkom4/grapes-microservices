# Fonction pour transformer les données
transform_data <- function(data) {
  # Filtrer les valeurs nulles ou négatives dans quantite
  data <- data %>% filter(quantite > 0)
  
  # Transformation logarithmique
  data <- data %>%
    mutate(log_quantite = log(quantite + 1), # +1 pour éviter log(0)
           log_prix_unitaire = log(prix_unitaire + 1))
  
  # Transformation quadratique
  data <- data %>%
    mutate(quantite2 = quantite^2,
           prix_unitaire2 = prix_unitaire^2)
  
  # Standardisation (Z-score)
  data <- data %>%
    mutate(quantite_std = (quantite - mean(quantite)) / sd(quantite),
           prix_unitaire_std = (prix_unitaire - mean(prix_unitaire)) / sd(prix_unitaire))
  
  return(data)
}

