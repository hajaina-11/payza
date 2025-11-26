-- =============================================
-- V4: Création de la table des transactions
-- =============================================

-- Table des transactions
CREATE TABLE pz_transaction (
                                id UUID PRIMARY KEY,
                                from_wallet_id UUID NOT NULL,
                                to_wallet_id UUID NOT NULL,
                                amount DECIMAL(19,2) NOT NULL,
                                fee DECIMAL(19,2) NOT NULL DEFAULT 0,
                                currency_code VARCHAR(10) NOT NULL,
                                type VARCHAR(50) NOT NULL,
                                status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
                                reference_encrypted TEXT NOT NULL,
                                description_encrypted TEXT,
                                created_at TIMESTAMP NOT NULL,
                                processed_at TIMESTAMP,
                                failure_reason TEXT,
                                metadata_encrypted TEXT,
                                ip_address_encrypted TEXT,
                                device_info_encrypted TEXT,

    -- Champs multi-devises
                                from_currency_code VARCHAR(10) NOT NULL DEFAULT 'MGA',
                                to_currency_code VARCHAR(10) NOT NULL DEFAULT 'MGA',
                                exchange_rate DECIMAL(19,8),
                                converted_amount DECIMAL(19,8),
                                from_currency_type VARCHAR(20) NOT NULL DEFAULT 'FIAT',
                                to_currency_type VARCHAR(20) NOT NULL DEFAULT 'FIAT',

    -- Champs crypto
                                blockchain_tx_hash_encrypted TEXT,
                                blockchain_confirmations INT,
                                crypto_wallet_address_encrypted TEXT
);

-- Index pour les performances
CREATE INDEX idx_transaction_from_wallet ON pz_transaction(from_wallet_id);
CREATE INDEX idx_transaction_to_wallet ON pz_transaction(to_wallet_id);
CREATE INDEX idx_transaction_status ON pz_transaction(status);
CREATE INDEX idx_transaction_created_at ON pz_transaction(created_at);
CREATE INDEX idx_transaction_currency_from ON pz_transaction(from_currency_code);
CREATE INDEX idx_transaction_currency_to ON pz_transaction(to_currency_code);
CREATE INDEX idx_transaction_currency_types ON pz_transaction(from_currency_type, to_currency_type);

-- Clés étrangères
ALTER TABLE pz_transaction
    ADD CONSTRAINT fk_transaction_from_wallet
        FOREIGN KEY (from_wallet_id) REFERENCES pz_wallet(id);

ALTER TABLE pz_transaction
    ADD CONSTRAINT fk_transaction_to_wallet
        FOREIGN KEY (to_wallet_id) REFERENCES pz_wallet(id);