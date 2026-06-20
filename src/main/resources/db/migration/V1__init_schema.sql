CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       first_name VARCHAR(100) NOT NULL,
                       last_name VARCHAR(100) NOT NULL,
                       email VARCHAR(150) NOT NULL UNIQUE,
                       phone VARCHAR(30),
                       created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE shipments (
                           id BIGSERIAL PRIMARY KEY,
                           tracking_number VARCHAR(30) NOT NULL UNIQUE,
                           user_id BIGINT NOT NULL REFERENCES users(id),
                           description VARCHAR(500) NOT NULL,
                           origin VARCHAR(200) NOT NULL,
                           destination VARCHAR(200) NOT NULL,
                           weight_kg NUMERIC(6,2),
                           current_status VARCHAR(30) NOT NULL DEFAULT 'CREATED',
                           created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                           updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_shipments_user ON shipments(user_id);
CREATE INDEX idx_shipments_status ON shipments(current_status);
CREATE INDEX idx_shipments_created_at ON shipments(created_at);

CREATE TABLE shipment_status_history (
                                         id BIGSERIAL PRIMARY KEY,
                                         shipment_id BIGINT NOT NULL REFERENCES shipments(id),
                                         status VARCHAR(30) NOT NULL,
                                         note VARCHAR(500),
                                         changed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                                         changed_by VARCHAR(150)
);

CREATE INDEX idx_status_history_shipment ON shipment_status_history(shipment_id);