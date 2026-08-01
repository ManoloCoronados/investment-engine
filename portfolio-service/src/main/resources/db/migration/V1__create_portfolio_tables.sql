-- Tabla 1: cola FIFO real — cada compra es un lote independiente
CREATE TABLE purchase_lots (
                               id                  BIGSERIAL PRIMARY KEY,
                               user_id             BIGINT NOT NULL,
                               symbol              VARCHAR(10) NOT NULL,
                               trade_id            VARCHAR(36) NOT NULL,        -- referencia al trade original
                               original_quantity   NUMERIC(18,4) NOT NULL,
                               remaining_quantity  NUMERIC(18,4) NOT NULL,       -- decrece cuando una venta lo consume
                               purchase_price      NUMERIC(18,4) NOT NULL,
                               purchased_at        TIMESTAMPTZ NOT NULL,
                               created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),

                               CONSTRAINT chk_remaining_not_negative CHECK (remaining_quantity >= 0),
                               CONSTRAINT chk_remaining_not_exceed CHECK (remaining_quantity <= original_quantity)
);

-- Idempotencia — el mismo trade no puede crear dos lotes
CREATE UNIQUE INDEX uq_purchase_lots_trade_id ON purchase_lots(trade_id);

-- El índice más importante de esta tabla: FIFO necesita
-- "los lotes más antiguos con remaining_quantity > 0, para este usuario+símbolo"
-- Este índice cubre exactamente esa consulta
CREATE INDEX idx_purchase_lots_fifo
    ON purchase_lots(user_id, symbol, purchased_at)
    WHERE remaining_quantity > 0;


-- Tabla 2: snapshot actual — una fila por usuario+símbolo
CREATE TABLE positions (
                           user_id         BIGINT NOT NULL,
                           symbol          VARCHAR(10) NOT NULL,
                           total_quantity  NUMERIC(18,4) NOT NULL DEFAULT 0,
                           average_cost    NUMERIC(18,4) NOT NULL DEFAULT 0,
                           updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),

                           PRIMARY KEY (user_id, symbol),
                           CONSTRAINT chk_total_quantity_not_negative CHECK (total_quantity >= 0)
);