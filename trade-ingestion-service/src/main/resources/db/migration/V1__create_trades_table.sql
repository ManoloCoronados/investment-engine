CREATE TABLE trades (
                        id              BIGSERIAL PRIMARY KEY,
                        trade_id        VARCHAR(36) NOT NULL,
                        user_id         BIGINT NOT NULL,
                        symbol          VARCHAR(10) NOT NULL,
                        quantity        NUMERIC(18,4) NOT NULL,
                        price           NUMERIC(18,4) NOT NULL,
                        trade_type      VARCHAR(4) NOT NULL,
                        executed_at     TIMESTAMPTZ NOT NULL,
                        created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),

                        CONSTRAINT chk_quantity_positive CHECK (quantity > 0),
                        CONSTRAINT chk_price_positive CHECK (price > 0),
                        CONSTRAINT chk_trade_type CHECK (trade_type IN ('BUY', 'SELL'))
);

CREATE UNIQUE INDEX uq_trades_trade_id ON trades(trade_id);

CREATE INDEX idx_trades_user_id ON trades(user_id);
CREATE INDEX idx_trades_user_executed ON trades(user_id, executed_at DESC);