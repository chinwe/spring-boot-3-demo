-- JOOQ 电商示例表结构

-- 用户表
CREATE TABLE IF NOT EXISTS j_users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 商品表
CREATE TABLE IF NOT EXISTS j_products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    price DECIMAL(10, 2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    category VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 订单表
CREATE TABLE IF NOT EXISTS j_orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    remarks VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES j_users(id)
);

-- 订单项表
CREATE TABLE IF NOT EXISTS j_order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    subtotal DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES j_orders(id),
    FOREIGN KEY (product_id) REFERENCES j_products(id)
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_orders_user_id ON j_orders(user_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON j_orders(status);
CREATE INDEX IF NOT EXISTS idx_orders_created_at ON j_orders(created_at);
CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON j_order_items(order_id);
CREATE INDEX IF NOT EXISTS idx_order_items_product_id ON j_order_items(product_id);
CREATE INDEX IF NOT EXISTS idx_products_category ON j_products(category);
