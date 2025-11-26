-- =============================================
-- V6: Données de référence pour les transactions
-- =============================================

-- Table des taux de change (pour les conversions multi-devises)
CREATE TABLE pz_exchange_rate (
                                  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                  from_currency VARCHAR(10) NOT NULL,
                                  to_currency VARCHAR(10) NOT NULL,
                                  rate DECIMAL(19,8) NOT NULL,
                                  source VARCHAR(50) NOT NULL DEFAULT 'MANUAL',
                                  last_updated TIMESTAMP NOT NULL DEFAULT NOW(),
                                  is_active BOOLEAN DEFAULT true,

                                  CONSTRAINT uq_exchange_rate_pair UNIQUE (from_currency, to_currency)
);

-- Index pour les taux de change
CREATE INDEX idx_exchange_rate_from ON pz_exchange_rate(from_currency);
CREATE INDEX idx_exchange_rate_to ON pz_exchange_rate(to_currency);
CREATE INDEX idx_exchange_rate_active ON pz_exchange_rate(is_active);

-- Insertion des taux de change de base
INSERT INTO pz_exchange_rate (from_currency, to_currency, rate, source) VALUES
-- Taux MGA (Ariary Malgache)
('MGA', 'EUR', 0.00020, 'MANUAL'),
('MGA', 'USD', 0.00022, 'MANUAL'),
('EUR', 'MGA', 5000.00, 'MANUAL'),
('USD', 'MGA', 4500.00, 'MANUAL'),

-- Taux des devises africaines
('XOF', 'EUR', 0.00152, 'MANUAL'),
('XAF', 'EUR', 0.00152, 'MANUAL'),
('ZAR', 'USD', 0.054, 'MANUAL'),
('KES', 'USD', 0.0078, 'MANUAL'),
('TZS', 'USD', 0.00043, 'MANUAL'),

-- Taux des cryptomonnaies (valeurs simulées)
('BTC', 'USD', 45000.00, 'MANUAL'),
('ETH', 'USD', 3000.00, 'MANUAL'),
('USDT', 'USD', 1.00, 'MANUAL'),
('BNB', 'USD', 350.00, 'MANUAL'),
('ADA', 'USD', 0.45, 'MANUAL'),
('DOT', 'USD', 7.50, 'MANUAL'),

-- Conversions croisées
('BTC', 'ETH', 15.00, 'MANUAL'),
('ETH', 'BTC', 0.0667, 'MANUAL'),

-- Conversions vers MGA
('BTC', 'MGA', 202500000.00, 'MANUAL'),
('ETH', 'MGA', 13500000.00, 'MANUAL');

-- Table de configuration des frais
CREATE TABLE pz_transaction_fee_config (
                                           id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                           transaction_type VARCHAR(50) NOT NULL,
                                           from_currency_type VARCHAR(20) NOT NULL,
                                           to_currency_type VARCHAR(20) NOT NULL,
                                           fee_rate DECIMAL(5,4) NOT NULL, -- Pourcentage (ex: 0.01 = 1%)
                                           min_fee DECIMAL(19,2) NOT NULL,
                                           max_fee DECIMAL(19,2),
                                           is_active BOOLEAN DEFAULT true,
                                           created_at TIMESTAMP NOT NULL DEFAULT NOW(),

                                           CONSTRAINT uq_fee_config_unique UNIQUE (transaction_type, from_currency_type, to_currency_type)
);

-- Index pour les frais
CREATE INDEX idx_fee_config_type ON pz_transaction_fee_config(transaction_type);
CREATE INDEX idx_fee_config_currency ON pz_transaction_fee_config(from_currency_type, to_currency_type);
CREATE INDEX idx_fee_config_active ON pz_transaction_fee_config(is_active);

-- Insertion des configurations de frais
INSERT INTO pz_transaction_fee_config (transaction_type, from_currency_type, to_currency_type, fee_rate, min_fee, max_fee) VALUES
-- Transactions FIAT standard
('TRANSFER', 'FIAT', 'FIAT', 0.0100, 10.00, 1000.00),
('WITHDRAWAL', 'FIAT', 'FIAT', 0.0200, 20.00, 2000.00),
('DEPOSIT', 'FIAT', 'FIAT', 0.0050, 5.00, 500.00),
('PAYMENT', 'FIAT', 'FIAT', 0.0150, 15.00, 1500.00),

-- Transactions crypto
('CRYPTO_TRANSFER', 'CRYPTO', 'CRYPTO', 0.0010, 0.0001, 10.00),
('CRYPTO_TRANSFER', 'FIAT', 'CRYPTO', 0.0200, 10.00, 1000.00),
('CRYPTO_TRANSFER', 'CRYPTO', 'FIAT', 0.0200, 10.00, 1000.00),

-- Conversions cross-devises
('CONVERSION', 'FIAT', 'FIAT', 0.0200, 20.00, 2000.00),
('CONVERSION', 'FIAT', 'CRYPTO', 0.0250, 25.00, 2500.00),
('CONVERSION', 'CRYPTO', 'FIAT', 0.0250, 25.00, 2500.00);

-- Vue pour faciliter les rapports sur les transactions
CREATE OR REPLACE VIEW vw_transaction_report AS
SELECT
    t.id,
    t.from_wallet_id,
    t.to_wallet_id,
    t.amount,
    t.converted_amount,
    t.fee,
    t.currency_code,
    t.from_currency_code,
    t.to_currency_code,
    t.exchange_rate,
    t.type,
    t.status,
    t.created_at,
    t.processed_at,
    t.failure_reason,
    t.from_currency_type,
    t.to_currency_type,
    -- Informations dérivées
    EXTRACT(YEAR FROM t.created_at) as transaction_year,
    EXTRACT(MONTH FROM t.created_at) as transaction_month,
    EXTRACT(DAY FROM t.created_at) as transaction_day,
    -- Statut simplifié
    CASE
        WHEN t.status IN ('COMPLETED', 'PROCESSING') THEN 'ACTIVE'
        WHEN t.status IN ('FAILED', 'CANCELLED') THEN 'INACTIVE'
        ELSE 'PENDING'
        END as status_category
FROM pz_transaction t;

-- Commentaires sur les nouvelles tables
COMMENT ON TABLE pz_exchange_rate IS 'Table des taux de change pour les conversions multi-devises';
COMMENT ON TABLE pz_transaction_fee_config IS 'Table de configuration des frais de transaction';
COMMENT ON VIEW vw_transaction_report IS 'Vue de rapport sur les transactions avec informations dérivées';