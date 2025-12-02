-- Initial migration: database schema creation
-- Cart and CartItem tables for managing shopping carts
-- Compatible with both H2 (dev/test) and PostgreSQL (production)
-- Note: UUID generation is handled by JPA @PrePersist, so no database default needed

-- Create carts table
-- Supports both guest carts (sessionId) and logged-in user carts (userId)
CREATE TABLE carts (
    id UUID PRIMARY KEY,
    user_id UUID,
    session_id VARCHAR2(255),
    site_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT carts_user_or_session CHECK (
        (user_id IS NOT NULL) OR (NULLIF(session_id, '') IS NOT NULL)
    )
);

-- Create cart_items table
CREATE TABLE cart_items (
    id UUID PRIMARY KEY,
    cart_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cart_items_cart FOREIGN KEY (cart_id)
        REFERENCES carts(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX idx_carts_user_id ON carts(user_id);
CREATE INDEX idx_carts_session_id ON carts(session_id);
CREATE INDEX idx_carts_site_id ON carts(site_id);
CREATE INDEX idx_carts_user_site ON carts(user_id, site_id);
CREATE INDEX idx_carts_session_site ON carts(session_id, site_id);
CREATE INDEX idx_cart_items_cart_id ON cart_items(cart_id);
CREATE INDEX idx_cart_items_product_id ON cart_items(product_id);
CREATE INDEX idx_cart_items_cart_product ON cart_items(cart_id, product_id);
