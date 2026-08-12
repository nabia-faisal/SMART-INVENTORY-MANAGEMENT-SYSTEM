USE inventory;

DROP TABLE IF EXISTS orderDetail;
DROP TABLE IF EXISTS product;
DROP TABLE IF EXISTS supplier_warehouse;
DROP TABLE IF EXISTS shelf;
DROP TABLE IF EXISTS supplier;
DROP TABLE IF EXISTS warehouse;
DROP TABLE IF EXISTS customer;
DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS appuser;

CREATE TABLE appuser (
    appuser_pk     INT AUTO_INCREMENT PRIMARY KEY,
    userRole       VARCHAR(50),
    name           VARCHAR(200),
    mobileNumber   VARCHAR(50),
    email          VARCHAR(200),
    password       VARCHAR(200),  
    address        VARCHAR(200),
    status         VARCHAR(50)     DEFAULT 'Active',
    failedAttempts INT             DEFAULT 0,
    otp            VARCHAR(10),
    otpExpiry      DATETIME,
    CONSTRAINT uq_appuser_email UNIQUE (email)
);

CREATE TABLE category (
    category_pk INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(200),
    -- MySQL's default collation is case-insensitive, so this UNIQUE
    -- constraint alone already rejects "Sports" after "sports" exists.
    -- The app also checks explicitly (Category.java) so the error message
    -- is friendly instead of a raw SQL exception.
    CONSTRAINT uq_category_name UNIQUE (name)
);

CREATE TABLE customer (
    customer_pk  INT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(200),
    mobileNumber VARCHAR(50),
    email        VARCHAR(200)
);

CREATE TABLE warehouse (
    warehouse_pk INT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(150) NOT NULL,
    city         VARCHAR(100),
    address      VARCHAR(200)
);

CREATE TABLE shelf (
    shelf_pk     INT AUTO_INCREMENT PRIMARY KEY,
    code         VARCHAR(20) NOT NULL,
    warehouse_fk INT NOT NULL,
    CONSTRAINT fk_shelf_warehouse FOREIGN KEY (warehouse_fk) REFERENCES warehouse(warehouse_pk) ON DELETE CASCADE,
    CONSTRAINT uq_shelf_per_warehouse UNIQUE (warehouse_fk, code)
);

CREATE TABLE supplier (
    supplier_pk   INT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(150) NOT NULL,
    contactPerson VARCHAR(100),
    phone         VARCHAR(50),
    email         VARCHAR(150),
    address       VARCHAR(200),
    CONSTRAINT uq_supplier_email UNIQUE (email)
);

CREATE TABLE supplier_warehouse (
    supplier_fk  INT NOT NULL,
    warehouse_fk INT NOT NULL,
    sinceDate    DATE,
    PRIMARY KEY (supplier_fk, warehouse_fk),
    CONSTRAINT fk_sw_supplier  FOREIGN KEY (supplier_fk)  REFERENCES supplier(supplier_pk)   ON DELETE CASCADE,
    CONSTRAINT fk_sw_warehouse FOREIGN KEY (warehouse_fk) REFERENCES warehouse(warehouse_pk) ON DELETE CASCADE
);

CREATE TABLE product (
    product_pk  INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(200),
    quantity    INT,
    price       INT,
    description VARCHAR(500),
    category_fk INT,
    shelf_fk    INT,  
    supplier_fk INT, 
    CONSTRAINT fk_product_category FOREIGN KEY (category_fk) REFERENCES category(category_pk) ON DELETE SET NULL,
    CONSTRAINT fk_product_shelf    FOREIGN KEY (shelf_fk)    REFERENCES shelf(shelf_pk)       ON DELETE SET NULL,
    CONSTRAINT fk_product_supplier FOREIGN KEY (supplier_fk) REFERENCES supplier(supplier_pk) ON DELETE SET NULL,
    CONSTRAINT chk_product_price    CHECK (price >= 0),
    CONSTRAINT chk_product_quantity CHECK (quantity >= 0)
);

CREATE TABLE orderDetail (
    order_pk    INT AUTO_INCREMENT PRIMARY KEY,
    orderId     VARCHAR(200),
    customer_fk INT,
    orderDate   DATE,
    totalPaid   INT,
    CONSTRAINT fk_order_customer FOREIGN KEY (customer_fk) REFERENCES customer(customer_pk) ON DELETE SET NULL,
    CONSTRAINT chk_order_totalpaid CHECK (totalPaid >= 0)
);

INSERT INTO appuser (userRole, name, mobileNumber, email, password, address, status)
VALUES ('SuperAdmin', 'SuperAdmin', '12345', 'superadmin@testemail.com', 'admin', 'Pakistan', 'Active');

INSERT INTO category (name) VALUES
('Electronics'),
('Office Supplies'),
('Furniture'),
('Kitchenware'),
('Stationery'),
('Tools & Hardware'),
('Cleaning Supplies'),
('Packaging Materials'),
('Sports & Outdoors'),
('Health & Safety'),
('Automotive Parts'),
('Textiles & Apparel');

INSERT INTO warehouse (name, city, address) VALUES
('Central Distribution Hub', 'Lahore', '12-B Industrial Estate, Kot Lakhpat'),
('North Regional Warehouse', 'Islamabad', 'Plot 45, Sector I-9 Industrial Area'),
('Port Storage Facility', 'Karachi', 'Warehouse 7, Bin Qasim Industrial Zone'),
('East Logistics Center', 'Faisalabad', '22-C Small Industrial Estate'),
('South Depot', 'Multan', 'Sundar Industrial Estate, Block 3'),
('Highland Storage Unit', 'Peshawar', 'Ring Road Industrial Area, Unit 9');

INSERT INTO shelf (code, warehouse_fk) VALUES
('A2', 1),
('A1', 1),
('B2', 1),
('B1', 1),
('A3', 2),
('A2', 2),
('C3', 3),
('A2', 3),
('C1', 3),
('A1', 3),
('A2', 4),
('B1', 4),
('C3', 5),
('D1', 5),
('C3', 6),
('B1', 6);

INSERT INTO supplier (name, contactPerson, phone, email, address) VALUES
('Alpha Traders Pvt Ltd', 'Ahmed Raza', '0300-1112233', 'sales@alphatraders.pk', 'Shahrah-e-Faisal, Karachi'),
('Metro Wholesale Suppliers', 'Bilal Sheikh', '0321-2223344', 'info@metrowholesale.pk', 'Gulberg III, Lahore'),
('Zenith Import Export', 'Sana Malik', '0333-3334455', 'contact@zenithie.pk', 'Blue Area, Islamabad'),
('Crescent Hardware Co.', 'Usman Tariq', '0345-4445566', 'orders@crescenthardware.pk', 'Jinnah Colony, Faisalabad'),
('Punjab General Supplies', 'Ayesha Khan', '0302-5556677', 'sales@punjabsupplies.pk', 'Multan Road, Multan'),
('Frontier Trading Company', 'Imran Wazir', '0313-6667788', 'info@frontiertrading.pk', 'University Road, Peshawar'),
('Skyline Distributors', 'Hina Farooq', '0334-7778899', 'hina@skylinedist.pk', 'DHA Phase 5, Lahore'),
('Reliance Office Solutions', 'Kamran Ali', '0301-8889900', 'kamran@relianceoffice.pk', 'F-8 Markaz, Islamabad'),
('National Packaging Ltd', 'Faisal Mahmood', '0322-9990011', 'faisal@nationalpack.pk', 'SITE Area, Karachi'),
('Horizon Textiles & Apparel', 'Nadia Yousaf', '0344-1011122', 'nadia@horizontextiles.pk', 'Faisal Town, Lahore'),
('Prime Auto Parts Corp', 'Waqas Ahmed', '0315-2122233', 'waqas@primeauto.pk', 'Ferozepur Road, Lahore'),
('SafeGuard Health Supplies', 'Mariam Siddiqui', '0336-3233344', 'mariam@safeguardhealth.pk', 'Clifton, Karachi'),
('Everest Sports Traders', 'Tariq Jameel', '0303-4344455', 'tariq@everestsports.pk', 'Model Town, Lahore'),
('BuildRight Tools & Fasteners', 'Zeeshan Qureshi', '0325-5455566', 'zeeshan@buildright.pk', 'Sundar Estate, Lahore');

INSERT INTO supplier_warehouse (supplier_fk, warehouse_fk, sinceDate) VALUES
(1, 6, '2025-01-15'),
(1, 5, '2025-05-10'),
(1, 4, '2025-07-01'),
(2, 1, '2025-09-18'),
(2, 2, '2025-05-10'),
(3, 3, '2025-01-15'),
(3, 2, '2026-01-05'),
(4, 1, '2025-05-10'),
(4, 6, '2024-11-02'),
(5, 3, '2025-03-20'),
(5, 5, '2026-01-05'),
(6, 6, '2025-05-10'),
(7, 1, '2025-07-01'),
(7, 4, '2025-03-20'),
(7, 6, '2026-01-05'),
(8, 5, '2025-09-18'),
(8, 3, '2024-11-02'),
(8, 2, '2024-11-02'),
(9, 2, '2026-01-05'),
(9, 3, '2025-01-15'),
(9, 1, '2026-01-05'),
(10, 4, '2025-03-20'),
(11, 6, '2025-01-15'),
(11, 3, '2025-03-20'),
(12, 2, '2025-09-18'),
(12, 3, '2025-09-18'),
(13, 1, '2025-07-01'),
(13, 5, '2025-09-18'),
(13, 2, '2025-01-15'),
(14, 4, '2025-05-10');

-- Total products generated: 100
INSERT INTO product (name, quantity, price, description, category_fk, shelf_fk, supplier_fk) VALUES
('USB-C Charging Cable', 143, 11285, 'USB-C Charging Cable - supplied item for electronics inventory', 1, 15, 11),
('Wireless Mouse', 171, 14609, 'Wireless Mouse - supplied item for electronics inventory', 1, 2, 14),
('Mechanical Keyboard', 21, 13989, 'Mechanical Keyboard - supplied item for electronics inventory', 1, 10, 5),
('27-inch LED Monitor', 38, 4256, '27-inch LED Monitor - supplied item for electronics inventory', 1, 14, 4),
('Bluetooth Speaker', 340, 8979, 'Bluetooth Speaker - supplied item for electronics inventory', 1, 12, 3),
('Power Bank 10000mAh', 140, 3087, 'Power Bank 10000mAh - supplied item for electronics inventory', 1, 6, 12),
('HDMI Cable 2m', 304, 7819, 'HDMI Cable 2m - supplied item for electronics inventory', 1, 14, 6),
('Laptop Cooling Pad', 117, 3066, 'Laptop Cooling Pad - supplied item for electronics inventory', 1, 14, 2),
('Webcam 1080p', 391, 1571, 'Webcam 1080p - supplied item for electronics inventory', 1, 2, 11),
('A4 Paper Ream', 86, 1779, 'A4 Paper Ream - supplied item for office supplies inventory', 2, 13, 7),
('Stapler Heavy Duty', 200, 2490, 'Stapler Heavy Duty - supplied item for office supplies inventory', 2, 12, 9),
('Whiteboard Markers Pack', 445, 97, 'Whiteboard Markers Pack - supplied item for office supplies inventory', 2, 15, 11),
('File Folder Set', 458, 2249, 'File Folder Set - supplied item for office supplies inventory', 2, 9, 2),
('Sticky Notes Pack', 155, 1830, 'Sticky Notes Pack - supplied item for office supplies inventory', 2, 6, 1),
('Correction Tape', 493, 1128, 'Correction Tape - supplied item for office supplies inventory', 2, 13, 9),
('Desk Organizer Tray', 472, 485, 'Desk Organizer Tray - supplied item for office supplies inventory', 2, 16, 14),
('Push Pins Box', 332, 2129, 'Push Pins Box - supplied item for office supplies inventory', 2, 13, 3),
('Rubber Bands Pack', 196, 711, 'Rubber Bands Pack - supplied item for office supplies inventory', 2, 13, 10),
('Ergonomic Office Chair', 170, 37021, 'Ergonomic Office Chair - supplied item for furniture inventory', 3, 1, 6),
('Adjustable Standing Desk', 454, 25153, 'Adjustable Standing Desk - supplied item for furniture inventory', 3, 5, 4),
('Bookshelf 5-Tier', 454, 42182, 'Bookshelf 5-Tier - supplied item for furniture inventory', 3, 1, 12),
('Filing Cabinet 4-Drawer', 253, 9535, 'Filing Cabinet 4-Drawer - supplied item for furniture inventory', 3, 13, 3),
('Conference Table', 342, 36148, 'Conference Table - supplied item for furniture inventory', 3, 13, 5),
('Reception Sofa', 275, 44753, 'Reception Sofa - supplied item for furniture inventory', 3, 11, 9),
('Storage Cabinet', 391, 18182, 'Storage Cabinet - supplied item for furniture inventory', 3, 16, 7),
('Cubicle Partition Panel', 348, 29472, 'Cubicle Partition Panel - supplied item for furniture inventory', 3, 12, 2),
('Ergonomic Office Chair (Batch 2)', 131, 19725, 'Ergonomic Office Chair (Batch 2) - supplied item for furniture inventory', 3, 3, 1),
('Non-Stick Frying Pan', 306, 4837, 'Non-Stick Frying Pan - supplied item for kitchenware inventory', 4, 5, 1),
('Stainless Steel Cookware Set', 41, 6098, 'Stainless Steel Cookware Set - supplied item for kitchenware inventory', 4, 15, 4),
('Electric Kettle', 39, 7717, 'Electric Kettle - supplied item for kitchenware inventory', 4, 3, 2),
('Cutlery Set 24pc', 268, 2249, 'Cutlery Set 24pc - supplied item for kitchenware inventory', 4, 10, 4),
('Food Storage Container Set', 281, 1383, 'Food Storage Container Set - supplied item for kitchenware inventory', 4, 16, 4),
('Blender 500W', 406, 4174, 'Blender 500W - supplied item for kitchenware inventory', 4, 11, 2),
('Coffee Maker', 54, 5698, 'Coffee Maker - supplied item for kitchenware inventory', 4, 12, 7),
('Dinner Plate Set', 215, 4125, 'Dinner Plate Set - supplied item for kitchenware inventory', 4, 15, 11),
('Non-Stick Frying Pan (Batch 2)', 339, 5593, 'Non-Stick Frying Pan (Batch 2) - supplied item for kitchenware inventory', 4, 1, 7),
('Ballpoint Pen Box', 377, 724, 'Ballpoint Pen Box - supplied item for stationery inventory', 5, 2, 4),
('Notebook A5 Ruled', 102, 1128, 'Notebook A5 Ruled - supplied item for stationery inventory', 5, 11, 7),
('Highlighter Set', 98, 600, 'Highlighter Set - supplied item for stationery inventory', 5, 11, 14),
('Mechanical Pencil Pack', 477, 184, 'Mechanical Pencil Pack - supplied item for stationery inventory', 5, 11, 1),
('Sketch Pad', 338, 1137, 'Sketch Pad - supplied item for stationery inventory', 5, 1, 13),
('Envelope Pack', 439, 514, 'Envelope Pack - supplied item for stationery inventory', 5, 6, 8),
('Sticky Flags Set', 251, 467, 'Sticky Flags Set - supplied item for stationery inventory', 5, 11, 3),
('Scissors Pack', 199, 34, 'Scissors Pack - supplied item for stationery inventory', 5, 12, 13),
('Cordless Drill Set', 406, 7654, 'Cordless Drill Set - supplied item for tools & hardware inventory', 6, 10, 12),
('Claw Hammer', 495, 8173, 'Claw Hammer - supplied item for tools & hardware inventory', 6, 5, 5),
('Adjustable Wrench Set', 116, 1158, 'Adjustable Wrench Set - supplied item for tools & hardware inventory', 6, 13, 12),
('Screwdriver Set 20pc', 165, 1136, 'Screwdriver Set 20pc - supplied item for tools & hardware inventory', 6, 4, 9),
('Measuring Tape 5m', 475, 8901, 'Measuring Tape 5m - supplied item for tools & hardware inventory', 6, 5, 9),
('Hex Key Set', 46, 3244, 'Hex Key Set - supplied item for tools & hardware inventory', 6, 1, 11),
('Utility Knife', 446, 4053, 'Utility Knife - supplied item for tools & hardware inventory', 6, 11, 10),
('Toolbox Organizer', 131, 851, 'Toolbox Organizer - supplied item for tools & hardware inventory', 6, 13, 7),
('All-Purpose Cleaner 1L', 341, 1295, 'All-Purpose Cleaner 1L - supplied item for cleaning supplies inventory', 7, 14, 5),
('Microfiber Cloth Pack', 109, 1471, 'Microfiber Cloth Pack - supplied item for cleaning supplies inventory', 7, 16, 4),
('Mop and Bucket Set', 140, 910, 'Mop and Bucket Set - supplied item for cleaning supplies inventory', 7, 6, 8),
('Disinfectant Spray', 166, 1639, 'Disinfectant Spray - supplied item for cleaning supplies inventory', 7, 1, 8),
('Trash Bags Roll', 323, 1253, 'Trash Bags Roll - supplied item for cleaning supplies inventory', 7, 1, 9),
('Glass Cleaner Spray', 114, 1136, 'Glass Cleaner Spray - supplied item for cleaning supplies inventory', 7, 8, 6),
('Floor Cleaner 5L', 456, 240, 'Floor Cleaner 5L - supplied item for cleaning supplies inventory', 7, 6, 5),
('Dustbin 20L', 85, 997, 'Dustbin 20L - supplied item for cleaning supplies inventory', 7, 14, 10),
('Corrugated Boxes (Medium)', 418, 2828, 'Corrugated Boxes (Medium) - supplied item for packaging materials inventory', 8, 13, 11),
('Bubble Wrap Roll', 423, 2421, 'Bubble Wrap Roll - supplied item for packaging materials inventory', 8, 7, 3),
('Packing Tape Roll', 140, 622, 'Packing Tape Roll - supplied item for packaging materials inventory', 8, 2, 5),
('Stretch Film Roll', 149, 2627, 'Stretch Film Roll - supplied item for packaging materials inventory', 8, 6, 4),
('Shipping Labels Pack', 356, 2747, 'Shipping Labels Pack - supplied item for packaging materials inventory', 8, 10, 5),
('Foam Padding Sheets', 468, 358, 'Foam Padding Sheets - supplied item for packaging materials inventory', 8, 4, 14),
('Poly Mailer Bags Pack', 146, 330, 'Poly Mailer Bags Pack - supplied item for packaging materials inventory', 8, 3, 13),
('Strapping Roll', 71, 2759, 'Strapping Roll - supplied item for packaging materials inventory', 8, 8, 12),
('Football Size 5', 231, 9338, 'Football Size 5 - supplied item for sports & outdoors inventory', 9, 16, 9),
('Yoga Mat', 9, 2132, 'Yoga Mat - supplied item for sports & outdoors inventory', 9, 2, 9),
('Adjustable Dumbbell Set', 23, 6349, 'Adjustable Dumbbell Set - supplied item for sports & outdoors inventory', 9, 13, 7),
('Cricket Bat', 70, 985, 'Cricket Bat - supplied item for sports & outdoors inventory', 9, 9, 13),
('Camping Tent 4-Person', 445, 953, 'Camping Tent 4-Person - supplied item for sports & outdoors inventory', 9, 8, 11),
('Water Bottle 1L', 132, 11227, 'Water Bottle 1L - supplied item for sports & outdoors inventory', 9, 3, 13),
('Badminton Racket Set', 291, 6958, 'Badminton Racket Set - supplied item for sports & outdoors inventory', 9, 13, 4),
('Skipping Rope', 447, 2962, 'Skipping Rope - supplied item for sports & outdoors inventory', 9, 6, 1),
('First Aid Kit', 96, 2821, 'First Aid Kit - supplied item for health & safety inventory', 10, 11, 5),
('Disposable Face Masks Box', 86, 985, 'Disposable Face Masks Box - supplied item for health & safety inventory', 10, 11, 14),
('Safety Gloves Pack', 245, 1922, 'Safety Gloves Pack - supplied item for health & safety inventory', 10, 6, 6),
('Hand Sanitizer 500ml', 161, 1964, 'Hand Sanitizer 500ml - supplied item for health & safety inventory', 10, 5, 11),
('Safety Goggles', 103, 3364, 'Safety Goggles - supplied item for health & safety inventory', 10, 9, 14),
('Reflective Safety Vest', 40, 2386, 'Reflective Safety Vest - supplied item for health & safety inventory', 10, 10, 11),
('Fire Extinguisher 2kg', 436, 4492, 'Fire Extinguisher 2kg - supplied item for health & safety inventory', 10, 7, 2),
('Ear Protection Muffs', 454, 2239, 'Ear Protection Muffs - supplied item for health & safety inventory', 10, 6, 1),
('Car Battery 12V', 60, 14739, 'Car Battery 12V - supplied item for automotive parts inventory', 11, 9, 7),
('Engine Oil Filter', 315, 17258, 'Engine Oil Filter - supplied item for automotive parts inventory', 11, 4, 10),
('Brake Pad Set', 102, 8846, 'Brake Pad Set - supplied item for automotive parts inventory', 11, 4, 1),
('Windshield Wiper Blades', 271, 18143, 'Windshield Wiper Blades - supplied item for automotive parts inventory', 11, 15, 6),
('Spark Plug Set', 225, 2792, 'Spark Plug Set - supplied item for automotive parts inventory', 11, 16, 10),
('Car Air Freshener', 165, 4583, 'Car Air Freshener - supplied item for automotive parts inventory', 11, 16, 9),
('Tire Pressure Gauge', 163, 13882, 'Tire Pressure Gauge - supplied item for automotive parts inventory', 11, 10, 12),
('Jumper Cables Set', 156, 18666, 'Jumper Cables Set - supplied item for automotive parts inventory', 11, 5, 7),
('Cotton T-Shirt Pack', 345, 3505, 'Cotton T-Shirt Pack - supplied item for textiles & apparel inventory', 12, 15, 10),
('Work Uniform Set', 296, 2865, 'Work Uniform Set - supplied item for textiles & apparel inventory', 12, 11, 5),
('Polyester Fabric Roll', 151, 2121, 'Polyester Fabric Roll - supplied item for textiles & apparel inventory', 12, 12, 8),
('Denim Fabric Roll', 231, 4022, 'Denim Fabric Roll - supplied item for textiles & apparel inventory', 12, 15, 9),
('Winter Jacket', 247, 1790, 'Winter Jacket - supplied item for textiles & apparel inventory', 12, 15, 5),
('Safety Boots Pair', 268, 5838, 'Safety Boots Pair - supplied item for textiles & apparel inventory', 12, 16, 2),
('Cap/Hat Pack', 424, 2324, 'Cap/Hat Pack - supplied item for textiles & apparel inventory', 12, 16, 4),
('Apron Set', 417, 2031, 'Apron Set - supplied item for textiles & apparel inventory', 12, 5, 1);
