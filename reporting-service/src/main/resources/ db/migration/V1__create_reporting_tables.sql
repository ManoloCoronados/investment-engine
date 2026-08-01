CREATE TABLE trades_view (
                             id            UUID PRIMARY KEY,
                             user_id       BIGINT NOT NULL,
                             symbol        VARCHAR(10) NOT NULL,
                             quantity      NUMERIC(18,4) NOT NULL,
                             price         NUMERIC(18,4) NOT NULL,
                             trade_type    VARCHAR(4) NOT NULL,
                             executed_at   TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_trades_view_user_date ON trades_view(user_id, executed_at DESC);

CREATE TABLE positions_view (
                                user_id         BIGINT NOT NULL,
                                symbol          VARCHAR(10) NOT NULL,
                                total_quantity  NUMERIC(18,4) NOT NULL,
                                average_cost    NUMERIC(18,4) NOT NULL,
                                updated_at      TIMESTAMPTZ NOT NULL,

                                PRIMARY KEY (user_id, symbol)
);

CREATE TABLE pnl_view (
                          user_id         BIGINT NOT NULL,
                          symbol          VARCHAR(10) NOT NULL,
                          unrealized_pnl  NUMERIC(18,4) NOT NULL,
                          current_price   NUMERIC(18,4) NOT NULL,
                          calculated_at   TIMESTAMPTZ NOT NULL,

                          PRIMARY KEY (user_id, symbol)
);