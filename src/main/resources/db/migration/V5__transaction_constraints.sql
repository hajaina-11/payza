-- =============================================
-- V5: Contraintes et validations pour les transactions
-- =============================================

-- Contrainte pour s'assurer que le montant est positif
ALTER TABLE pz_transaction
    ADD CONSTRAINT chk_transaction_amount_positive
        CHECK (amount > 0);

-- Contrainte pour s'assurer que les frais ne sont pas négatifs
ALTER TABLE pz_transaction
    ADD CONSTRAINT chk_transaction_fee_non_negative
        CHECK (fee >= 0);

-- Contrainte pour valider le statut de la transaction
ALTER TABLE pz_transaction
    ADD CONSTRAINT chk_transaction_status_valid
        CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'CANCELLED', 'REVERSED'));

-- Contrainte pour valider le type de transaction
ALTER TABLE pz_transaction
    ADD CONSTRAINT chk_transaction_type_valid
        CHECK (type IN ('TRANSFER', 'DEPOSIT', 'WITHDRAWAL', 'PAYMENT', 'REFUND', 'FEE', 'CRYPTO_TRANSFER', 'CONVERSION'));

-- Contrainte pour valider les types de devise
ALTER TABLE pz_transaction
    ADD CONSTRAINT chk_transaction_currency_type_valid
        CHECK (from_currency_type IN ('FIAT', 'CRYPTO') AND to_currency_type IN ('FIAT', 'CRYPTO'));

-- Contrainte pour s'assurer que processed_at est après created_at si présent
ALTER TABLE pz_transaction
    ADD CONSTRAINT chk_transaction_dates_valid
        CHECK (processed_at IS NULL OR processed_at >= created_at);

-- Contrainte pour s'assurer que le taux de change est positif si présent
ALTER TABLE pz_transaction
    ADD CONSTRAINT chk_transaction_exchange_rate_positive
        CHECK (exchange_rate IS NULL OR exchange_rate > 0);

-- Index supplémentaires pour les performances des requêtes courantes
CREATE INDEX idx_transaction_user_search ON pz_transaction (from_wallet_id, created_at DESC);
CREATE INDEX idx_transaction_status_date ON pz_transaction (status, created_at);
CREATE INDEX idx_transaction_type_status ON pz_transaction (type, status);

-- Index pour les recherches par période
CREATE INDEX idx_transaction_created_year_month ON pz_transaction (EXTRACT(YEAR FROM created_at), EXTRACT(MONTH FROM created_at));

-- Commentaires sur la table et les colonnes
COMMENT ON TABLE pz_transaction IS 'Table des transactions financières avec support multi-devises';
COMMENT ON COLUMN pz_transaction.reference_encrypted IS 'Référence unique de la transaction (cryptée)';
COMMENT ON COLUMN pz_transaction.description_encrypted IS 'Description de la transaction (cryptée)';
COMMENT ON COLUMN pz_transaction.metadata_encrypted IS 'Métadonnées supplémentaires (cryptées)';
COMMENT ON COLUMN pz_transaction.ip_address_encrypted IS 'Adresse IP de l initiateur (cryptée)';
COMMENT ON COLUMN pz_transaction.device_info_encrypted IS 'Informations sur l appareil (cryptées)';
COMMENT ON COLUMN pz_transaction.blockchain_tx_hash_encrypted IS 'Hash de la transaction blockchain (crypté)';
COMMENT ON COLUMN pz_transaction.crypto_wallet_address_encrypted IS 'Adresse du wallet crypto (cryptée)';
COMMENT ON COLUMN pz_transaction.exchange_rate IS 'Taux de change utilisé pour la conversion';
COMMENT ON COLUMN pz_transaction.converted_amount IS 'Montant converti dans la devise de destination';