-- Create the database
CREATE DATABASE IF NOT EXISTS sales_db;
CREATE DATABASE IF NOT EXISTS payment_db;
CREATE DATABASE IF NOT EXISTS BD_OPER_ACTIVITIES;


## DATA-MINING
-- Ensure using the correct database (MariaDB env var should create it)
USE BD_OPER_ACTIVITIES;

-- Drop tables first IF THEY EXIST (for clean initialization)
-- Important: Only runs if the script executes multiple times on non-empty volume somehow.
-- Usually these images run scripts only on first init. Add safeguards if needed.
SET FOREIGN_KEY_CHECKS=0; -- Disable temporarily
DROP TABLE IF EXISTS service_usage;
DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS services;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS clients;
SET FOREIGN_KEY_CHECKS=1; -- Re-enable

-- Create clients table
CREATE TABLE clients (
                         client_id INT AUTO_INCREMENT PRIMARY KEY,
                         region VARCHAR(100),
                         age INT NULL,
                         prefere_produit_local BOOLEAN NULL,
                         signup_date DATE NULL
);
-- Create products table
CREATE TABLE products (
                          product_id INT AUTO_INCREMENT PRIMARY KEY,
                          name VARCHAR(255) NOT NULL,
                          saison VARCHAR(50),
                          is_local BOOLEAN NULL,
                          base_price DECIMAL(10, 2),
                          category VARCHAR(100)
);
-- Create services table (with corrected UNIQUE key)
CREATE TABLE services (
                          service_id INT AUTO_INCREMENT PRIMARY KEY,
                          name VARCHAR(255) NOT NULL,
                          category VARCHAR(100),
                          UNIQUE KEY `uniq_service_name` (`name`(191)) -- Prefixed unique key
);
-- Create transactions table
CREATE TABLE transactions (
                              transaction_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              client_id INT NOT NULL,
                              product_id INT NULL,
                              service_id INT NULL,
                              quantity INT,
                              unit_price DECIMAL(10, 2),
                              total_amount DECIMAL(12, 2) NOT NULL,
                              transaction_timestamp DATETIME NOT NULL,
                              payment_method VARCHAR(50),
                              payment_status VARCHAR(20) NOT NULL DEFAULT 'Success',
                              delivery_status VARCHAR(50),
                              delivery_time_days INT NULL,
                              source_system VARCHAR(50),
                              INDEX idx_client (client_id),
                              INDEX idx_timestamp (transaction_timestamp),
                              FOREIGN KEY (client_id) REFERENCES clients(client_id),
                              FOREIGN KEY (product_id) REFERENCES products(product_id),
                              FOREIGN KEY (service_id) REFERENCES services(service_id)
);
-- Create service_usage table
CREATE TABLE service_usage (
                               usage_log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               client_id INT NOT NULL,
                               service_id INT NOT NULL,
                               usage_timestamp DATETIME NOT NULL,
                               request_details TEXT NULL,
                               status VARCHAR(20) DEFAULT 'Completed',
                               duration_ms INT NULL,
                               INDEX idx_su_client (client_id),
                               INDEX idx_su_timestamp (usage_timestamp),
                               FOREIGN KEY (client_id) REFERENCES clients(client_id),
                               FOREIGN KEY (service_id) REFERENCES services(service_id)
);

-- Optional: Add some initial sample data if needed for testing/dev
-- INSERT INTO clients (region, age, ...) VALUES (...);


-- Supprimer le user s’il existe déjà
DROP USER IF EXISTS 'adminMasi'@'%';

-- Recréer proprement l'utilisateur avec un mot de passe
CREATE USER 'adminMasi'@'%' IDENTIFIED BY '@adminMASI';

-- Lui accorder tous les droits sur la base
GRANT ALL PRIVILEGES ON *.* TO 'adminMasi'@'%' IDENTIFIED BY '@adminMASI' WITH GRANT OPTION;

FLUSH PRIVILEGES;


