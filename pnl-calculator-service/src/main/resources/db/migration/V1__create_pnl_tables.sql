-- Copia local de las posiciones — alimentada por portfolio.updated
CREATE TABLE positions_snapshot (
                                    user_id         BIGINT NOT NULL,
                                    symbol          VARCHAR(10) NOT NULL,
                                    total_quantity  NUMERIC(18,4) NOT NULL,
                                    average_cost    NUMERIC(18,4) NOT NULL,
                                    updated_at      TIMESTAMPTZ NOT NULL,

                                    PRIMARY KEY (user_id, symbol)
);

-- Copia local del último precio conocido — alimentada por prices.updated
CREATE TABLE latest_prices (
                               symbol      VARCHAR(10) PRIMARY KEY,
                               price       NUMERIC(18,4) NOT NULL,
                               updated_at  TIMESTAMPTZ NOT NULL
);