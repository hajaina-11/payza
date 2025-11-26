-- Lier les wallets aux users
ALTER TABLE pz_wallet
    ADD CONSTRAINT fk_wallet_user
    FOREIGN KEY (ownerid) REFERENCES pz_user(id);