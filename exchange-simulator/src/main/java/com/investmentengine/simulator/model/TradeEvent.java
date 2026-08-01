package com.investmentengine.simulator.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TradeEvent(

        String tradeId,
        ///Identificador unico de el trade, lo usare en Kafka Idempotencia

        Long userId,
        ///ID del usuario inversionista

        String symbol,
        ///  // Símbolo de la acción — AAPL, TSLA, GOOGL, etc.


        BigDecimal quantity,
        /// Cantidad de acciones operadas


        BigDecimal price,
        ///Precio por acción en el momento del trade


        TradeType tradeType,
        ///BUY o SELL tipo de trade si vender o comprar



        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Instant executedAt
        ///TIEMPO exacto donde ocurrio el trend
)

{
    /// Enum anidado — forma limpia de representar los dos tipos posibles
    public enum TradeType {
         BUY,
         SELL
}


    /** Factory metodo es un metodo que llevan los campos que se enviara por el event kafka
     mas tarde en service le pondre implementacion */


    public  static  TradeEvent random(Long userId, String symbol,
                            BigDecimal price, BigDecimal quantity,
                            TradeType tradeType) {

        return new TradeEvent(
                UUID.randomUUID().toString(),  // tradeId único garantizado
                userId,
                symbol,
                quantity,
                price,
                tradeType,
                Instant.now()
        );
    }





}
