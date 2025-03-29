-- Create the database
CREATE DATABASE IF NOT EXISTS sales_db;
USE sales_db;

SET FOREIGN_KEY_CHECKS = 0;

-- Test data insertion
INSERT INTO category (name, description, picture_path)
VALUES ('Fruits', 'Fresh seasonal fruits', 'fruits.jpg');

INSERT INTO family (name, description)
VALUES ('Citrus', 'Citrus fruits family');

INSERT INTO article (category_id, family_id, name, description, price_kg, price_unit, stock_kg, unit_stock, origin, picture_path)
VALUES (1, 1, 'Orange', 'Sweet orange', 2.50, 0.60, 100, 200, 'Spain', 'orange.jpg');

INSERT INTO `order` (code, user_id, facture_path, total_price, created_at, is_paid, is_finished)
VALUES (1234, 1, 'facture_1234.pdf', 25.00, NOW(), TRUE, FALSE);

INSERT INTO order_item (article_id, order_id, cart_id, price, quantity_kg, quantity, scanned_at, is_scanned)
VALUES (1, 1, 1, 2.50, 5.0, 10, NOW(), TRUE);

INSERT INTO delivery_status (label)
VALUES ('En cours');

INSERT INTO delivery (order_id, user_id, delivery_status_id, delivery_date, delivered_at, scanned_at, tracking_url, comment, doorstep, signature)
VALUES (1, 1, 1, NOW(), NULL, NULL, 'https://tracking.test/1234', 'Leave at door', TRUE, NULL);

INSERT INTO delivery_picture (delivery_id, picture_path)
VALUES (1, 'delivered.jpg');

SET FOREIGN_KEY_CHECKS = 1;
