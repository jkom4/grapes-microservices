INSERT INTO category (id, name, description, picture_path)
VALUES (1, 'Légumes', 'Catégorie des légumes frais', '/images/legumes.jpg');

INSERT INTO family (id, name, description)
VALUES (1, 'Fruits & Légumes', 'Famille regroupant fruits et légumes');

INSERT INTO article (
    id, category_id, family_id, name, description, price_kg, price_unit,
    stock_kg, unit_stock, origin, picture_path
) VALUES (
             1, 1, 1, 'Tomate', 'Tomates fraîches bio', 1.25, 0.80,
             100.0, 50.0, 'Belgique', '/images/tomate.jpg'
         );
