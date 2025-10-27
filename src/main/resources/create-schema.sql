DROP TABLE IF EXISTS donations CASCADE;

CREATE TABLE donations
(
    uuid                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    donor_name          VARCHAR(255)   NOT NULL,
    donor_email         VARCHAR(255)   NOT NULL,
    amount              DECIMAL(19, 2) NOT NULL,
    currency            VARCHAR(3)     NOT NULL DEFAULT 'EUR',
    message             TEXT,
    created_at          TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP,
    paypal_order_id     VARCHAR(255),
    paypal_capture_id   VARCHAR(255),
    paypal_email        VARCHAR(255),
    paypal_payer_id     VARCHAR(255),
    paypal_approve_link VARCHAR(255),
    paypal_status       VARCHAR(50),
    paypal_response_data TEXT,
    paypal_error_message TEXT,
    paypal_created_at   TIMESTAMP,
    paypal_updated_at   TIMESTAMP,

    CONSTRAINT amount_positive CHECK (amount > 0)
);

CREATE INDEX IF NOT EXISTS idx_donor_email ON donations (donor_email);
CREATE INDEX IF NOT EXISTS idx_paypal_order_id ON donations (paypal_order_id);
CREATE INDEX IF NOT EXISTS idx_paypal_status ON donations (paypal_status);
CREATE INDEX IF NOT EXISTS idx_created_at ON donations (created_at DESC);