CREATE TABLE market_prices (
                               id          BIGSERIAL PRIMARY KEY,
                               symbol      VARCHAR(10) NOT NULL,
                               price       NUMERIC(18,4) NOT NULL,
                               recorded_at TIMESTAMPTZ NOT NULL DEFAULT now(),

                               CONSTRAINT chk_price_positive CHECK (price > 0)
);

CREATE INDEX idx_market_prices_symbol_recorded
    ON market_prices(symbol, recorded_at DESC);