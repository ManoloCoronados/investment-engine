CREATE TABLE dead_letter_trades (
                                    id            BIGSERIAL PRIMARY KEY,
                                    trade_id      VARCHAR(36) NOT NULL,
                                    user_id       BIGINT NOT NULL,
                                    symbol        VARCHAR(10) NOT NULL,
                                    payload       TEXT NOT NULL,
                                    error_message TEXT NOT NULL,
                                    retry_count   INT NOT NULL DEFAULT 0,
                                    status        VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                                    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
                                    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_dlq_status ON dead_letter_trades(status);
CREATE INDEX idx_dlq_trade_id ON dead_letter_trades(trade_id);