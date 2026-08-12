USE inventory;

DROP FUNCTION IF EXISTS fun_getStockValue;
DROP FUNCTION IF EXISTS fun_getWarehouseCountForSupplier;
DROP FUNCTION IF EXISTS fun_stockStatus;

DELIMITER $$

-- Returns price * quantity for a single product.
CREATE FUNCTION fun_getStockValue(p_product_id INT)
RETURNS DECIMAL(14,2)
DETERMINISTIC
BEGIN
    DECLARE v_value DECIMAL(14,2);
    SELECT price * quantity INTO v_value
    FROM product
    WHERE product_pk = p_product_id;
    RETURN IFNULL(v_value, 0);
END$$

-- Returns how many warehouses a supplier currently delivers to
-- (counts rows in the supplier_warehouse junction table).
CREATE FUNCTION fun_getWarehouseCountForSupplier(p_supplier_id INT)
RETURNS INT
DETERMINISTIC
BEGIN
    DECLARE v_count INT;
    SELECT COUNT(*) INTO v_count
    FROM supplier_warehouse
    WHERE supplier_fk = p_supplier_id;
    RETURN v_count;
END$$

CREATE FUNCTION fun_stockStatus(p_quantity INT)
RETURNS VARCHAR(20)
DETERMINISTIC
BEGIN
    RETURN CASE
        WHEN p_quantity <= 0  THEN 'Out of Stock'
        WHEN p_quantity < 20  THEN 'Low'
        WHEN p_quantity < 100 THEN 'Medium'
        ELSE 'High'
    END;
END$$

DELIMITER ;


DROP PROCEDURE IF EXISTS sp_restock_product;
DROP PROCEDURE IF EXISTS sp_get_low_stock_products;
DROP PROCEDURE IF EXISTS sp_get_inventory_value;
DROP PROCEDURE IF EXISTS sp_transfer_product_shelf;
DROP PROCEDURE IF EXISTS sp_apply_bulk_discount;
DROP PROCEDURE IF EXISTS sp_generate_shelves;
DROP PROCEDURE IF EXISTS sp_get_supplier_stock_value;

DELIMITER $$

CREATE PROCEDURE sp_restock_product(IN p_product_id INT, IN p_quantity INT)
BEGIN
    IF p_quantity <= 0 THEN
        SELECT 'Quantity must be positive' AS message;
    ELSE
        UPDATE product SET quantity = quantity + p_quantity WHERE product_pk = p_product_id;
        SELECT 'Stock updated successfully' AS message;
    END IF;
END$$

CREATE PROCEDURE sp_get_low_stock_products(IN p_threshold INT)
BEGIN
    SELECT product_pk, name, quantity, fun_stockStatus(quantity) AS status
    FROM product
    WHERE quantity < p_threshold
    ORDER BY quantity;
END$$

CREATE PROCEDURE sp_get_inventory_value(OUT p_total_value DECIMAL(14,2))
BEGIN
    SELECT SUM(price * quantity) INTO p_total_value FROM product;
END$$

-- OUT parameter + IF validation: move a product to a different shelf.
CREATE PROCEDURE sp_transfer_product_shelf(
    IN p_product_id INT,
    IN p_new_shelf_id INT,
    OUT p_status VARCHAR(100)
)
BEGIN
    DECLARE v_shelf_exists INT;
    SELECT COUNT(*) INTO v_shelf_exists FROM shelf WHERE shelf_pk = p_new_shelf_id;

    IF v_shelf_exists = 0 THEN
        SET p_status = 'Shelf does not exist';
    ELSE
        UPDATE product SET shelf_fk = p_new_shelf_id WHERE product_pk = p_product_id;
        SET p_status = 'Product relocated successfully';
    END IF;
END$$

CREATE PROCEDURE sp_apply_bulk_discount(
    IN p_category_id INT,
    INOUT p_discount_pct DECIMAL(5,2)
)
BEGIN
    IF p_discount_pct > 50 THEN
        SET p_discount_pct = 50;
    END IF;

    UPDATE product
    SET price = price - (price * p_discount_pct / 100)
    WHERE category_fk = p_category_id;
END$$

CREATE PROCEDURE sp_generate_shelves(IN p_warehouse_id INT, IN p_count INT)
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE v_code VARCHAR(20);

    WHILE i <= p_count DO
        SET v_code = CONCAT('AUTO-', i);
        INSERT INTO shelf(code, warehouse_fk) VALUES (v_code, p_warehouse_id);
        SET i = i + 1;
    END WHILE;
END$$

CREATE PROCEDURE sp_get_supplier_stock_value(IN p_supplier_id INT, OUT p_total_value DECIMAL(14,2))
BEGIN
    DECLARE v_value DECIMAL(14,2);
    SELECT SUM(price * quantity) INTO v_value FROM product WHERE supplier_fk = p_supplier_id;
    SET p_total_value = IFNULL(v_value, 0);
END$$

DELIMITER ;

DROP TABLE IF EXISTS product_log;
CREATE TABLE product_log (
    log_id      INT AUTO_INCREMENT PRIMARY KEY,
    action_type VARCHAR(50),
    old_value   VARCHAR(300),
    new_value   VARCHAR(300),
    action_date DATETIME DEFAULT CURRENT_TIMESTAMP
);

DROP TRIGGER IF EXISTS trg_before_insert_product;
DROP TRIGGER IF EXISTS trg_after_insert_product;
DROP TRIGGER IF EXISTS trg_before_update_product;
DROP TRIGGER IF EXISTS trg_after_delete_product;

DELIMITER $$

CREATE TRIGGER trg_before_insert_product
BEFORE INSERT ON product
FOR EACH ROW
BEGIN
    INSERT INTO product_log(action_type, old_value, new_value)
    VALUES ('BEFORE INSERT', NULL, CONCAT('Name set as ', NEW.name));
END$$

CREATE TRIGGER trg_after_insert_product
AFTER INSERT ON product
FOR EACH ROW
BEGIN
    INSERT INTO product_log(action_type, old_value, new_value)
    VALUES ('AFTER INSERT', NULL, CONCAT('Inserted ', NEW.name, ' with price ', NEW.price));
END$$

CREATE TRIGGER trg_before_update_product
BEFORE UPDATE ON product
FOR EACH ROW
BEGIN
    INSERT INTO product_log(action_type, old_value, new_value)
    VALUES (
        'BEFORE UPDATE',
        CONCAT('Name:', OLD.name, ', Price:', OLD.price, ', Qty:', OLD.quantity),
        CONCAT('Name:', NEW.name, ', Price:', NEW.price, ', Qty:', NEW.quantity)
    );
END$$

CREATE TRIGGER trg_after_delete_product
AFTER DELETE ON product
FOR EACH ROW
BEGIN
    INSERT INTO product_log(action_type, old_value, new_value)
    VALUES ('AFTER DELETE', CONCAT('Name:', OLD.name, ', Price:', OLD.price), NULL);
END$$

DELIMITER ;


DROP VIEW IF EXISTS low_stock_vu;
DROP VIEW IF EXISTS product_catalog_vu;
DROP VIEW IF EXISTS supplier_warehouse_summary_vu;
DROP VIEW IF EXISTS top5_products_by_value_vu;
DROP VIEW IF EXISTS karachi_warehouse_vu;
DROP VIEW IF EXISTS suppliers_without_products_vu;

CREATE VIEW low_stock_vu AS
SELECT product_pk, name, quantity, price
FROM product
WHERE quantity < 20;

CREATE VIEW product_catalog_vu AS
SELECT
    p.product_pk,
    p.name AS product_name,
    c.name AS category_name,
    p.price,
    p.quantity,
    IFNULL(w.name, 'Unassigned') AS warehouse_name,
    IFNULL(s.code, 'Unassigned') AS shelf_code,
    IFNULL(sup.name, 'No Supplier') AS supplier_name
FROM product p
INNER JOIN category c ON p.category_fk = c.category_pk
LEFT JOIN shelf s      ON p.shelf_fk = s.shelf_pk
LEFT JOIN warehouse w  ON s.warehouse_fk = w.warehouse_pk
LEFT JOIN supplier sup ON p.supplier_fk = sup.supplier_pk;

CREATE VIEW supplier_warehouse_summary_vu AS
SELECT
    sup.supplier_pk,
    sup.name AS supplier_name,
    COUNT(sw.warehouse_fk) AS warehouse_count
FROM supplier sup
LEFT JOIN supplier_warehouse sw ON sup.supplier_pk = sw.supplier_fk
GROUP BY sup.supplier_pk, sup.name;

CREATE VIEW top5_products_by_value_vu AS
SELECT product_pk, name, price, quantity, (price * quantity) AS stock_value
FROM product
ORDER BY stock_value DESC
LIMIT 5;

CREATE VIEW karachi_warehouse_vu AS
SELECT warehouse_pk, name, city, address
FROM warehouse
WHERE city = 'Karachi'
WITH CHECK OPTION;

CREATE VIEW suppliers_without_products_vu AS
SELECT s.supplier_pk, s.name
FROM supplier s
WHERE NOT EXISTS (
    SELECT 1 FROM product p WHERE p.supplier_fk = s.supplier_pk
);