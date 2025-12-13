INSERT INTO products (product_id, name, product_type, price, stock_quantity, metadata) VALUES
('BOOK-CC-001', 'Clean Code', 'PHYSICAL', 89.90, 150, NULL),
('LAPTOP-PRO-2024', 'Laptop Pro', 'PHYSICAL', 5499.00, 8, NULL),
('LAPTOP-MBP-M3-001', 'MacBook Pro M3', 'PHYSICAL', 12999.00, 25, NULL),

('SUB-PREMIUM-001', 'Premium Monthly', 'SUBSCRIPTION', 49.90, NULL, NULL),
('SUB-BASIC-001', 'Basic Monthly', 'SUBSCRIPTION', 19.90, NULL, NULL),
('SUB-ENTERPRISE-001', 'Enterprise Plan', 'SUBSCRIPTION', 299.00, NULL, NULL),
('SUB-ADOBE-CC-001', 'Adobe Creative Cloud', 'SUBSCRIPTION', 159.00, NULL, NULL),

('EBOOK-JAVA-001', 'Effective Java', 'DIGITAL', 39.90, 1000, '{"licensesAvailable": 1000}'::jsonb),
('EBOOK-DDD-001', 'Domain-Driven Design', 'DIGITAL', 59.90, 500, '{"licensesAvailable": 500}'::jsonb),
('EBOOK-SWIFT-001', 'Swift Programming', 'DIGITAL', 49.90, 800, '{"licensesAvailable": 800}'::jsonb),
('COURSE-KAFKA-001', 'Kafka Mastery', 'DIGITAL', 299.00, 500, '{"licensesAvailable": 500}'::jsonb),

('GAME-2025-001', 'Epic Game 2025', 'PRE_ORDER', 249.90, 1000, '{"releaseDate": "2025-06-01", "preOrderSlots": 1000}'::jsonb),
('PRE-PS6-001', 'PlayStation 6', 'PRE_ORDER', 4999.00, 500, '{"releaseDate": "2025-11-15", "preOrderSlots": 500}'::jsonb),
('PRE-IPHONE16-001', 'iPhone 16 Pro', 'PRE_ORDER', 7999.00, 2000, '{"releaseDate": "2025-09-20", "preOrderSlots": 2000}'::jsonb),

('CORP-LICENSE-ENT', 'Enterprise License', 'CORPORATE', 15000.00, NULL, NULL),
('CORP-CHAIR-ERG-001', 'Ergonomic Chair Bulk', 'CORPORATE', 899.00, 500, NULL);