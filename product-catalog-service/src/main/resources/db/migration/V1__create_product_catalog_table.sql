CREATE TABLE products (
  product_id VARCHAR(50) PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  product_type VARCHAR(20) NOT NULL,
  price DECIMAL(10, 2) NOT NULL,
  stock_quantity INTEGER,
  active BOOLEAN DEFAULT true,
  metadata JSONB,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,
  updated_at TIMESTAMP WITH TIME ZONE
);