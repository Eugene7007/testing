CREATE TABLE departments (
                             id BIGSERIAL PRIMARY KEY,
                             name VARCHAR(50) NOT NULL,
                             location VARCHAR(50)
);

CREATE TABLE employees (
                           id BIGSERIAL PRIMARY KEY,
                           name VARCHAR(50) NOT NULL,
                           position VARCHAR(50),
                           salary BIGINT CHECK (salary >= 0),
                           department_id BIGINT REFERENCES departments(id) ON DELETE SET NULL,
                           manager_id BIGINT REFERENCES employees(id) ON DELETE SET NULL
);

CREATE TABLE customers (
                           id BIGSERIAL PRIMARY KEY,
                           name VARCHAR(100) NOT NULL,
                           city VARCHAR(50)
);

CREATE TABLE orders (
                        id BIGSERIAL PRIMARY KEY,
                        created_at timestamp NOT NULL,
                        updated_at timestamp NOT NULL,
                        amount BIGINT NOT NULL,
                        employee_id BIGINT REFERENCES employees(id) ON DELETE SET NULL,
                        customer_id BIGINT REFERENCES customers(id) ON DELETE SET NULL
);

CREATE TABLE products (
                          id BIGSERIAL PRIMARY KEY,
                          name VARCHAR(100) NOT NULL,
                          price BIGINT NOT NULL CHECK (price >= 0)
);

CREATE TABLE order_items (
                             id BIGSERIAL PRIMARY KEY,
                             order_id BIGINT REFERENCES orders(id) ON DELETE CASCADE,
                             product_id BIGINT REFERENCES products(id) ON DELETE SET NULL,
                             quantity BIGINT NOT NULL CHECK (quantity > 0)
);

CREATE INDEX idx_employees_department_id ON employees(department_id);
CREATE INDEX idx_employees_manager_id ON employees(manager_id);
CREATE INDEX idx_orders_employee_id ON orders(employee_id);
CREATE INDEX idx_orders_customer_id ON orders(customer_id);
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_product_id ON order_items(product_id);
