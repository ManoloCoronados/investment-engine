package com.investmentengine.portfolio.service;

import com.investmentengine.portfolio.model.*;
import com.investmentengine.portfolio.producer.PortfolioUpdatedProducer;
import com.investmentengine.portfolio.repository.PositionRepository;
import com.investmentengine.portfolio.repository.PurchaseLotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FifoPortfolioService {

    private final PurchaseLotRepository lotRepository;
    private final PositionRepository positionRepository;
    private final PortfolioUpdatedProducer producer;

    @Transactional
    public void process(TradeEvent event) {

        if (lotRepository.existsByTradeId(event.tradeId())) {
            log.warn("Trade ya procesado en portfolio-service [tradeId={}]", event.tradeId());
            return;
        }


        /**
         * Comienza en 0 / perdida o ganancia
         */
        BigDecimal realizedPnl = null;

        if (event.tradeType() == TradeEvent.TradeType.BUY) {
            handleBuy(event);
        } else {

            realizedPnl = handleSell(event);
        }


        Position updatedPosition = recalculatePosition(event.userId(), event.symbol());

        PortfolioUpdatedEvent updatedEvent = new PortfolioUpdatedEvent(
                event.userId(),
                event.symbol(),
                event.tradeId(),
                updatedPosition.totalQuantity(),
                updatedPosition.averageCost(),
                realizedPnl,
                Instant.now()
        );

        producer.publish(updatedEvent);


    }

    private void handleBuy(TradeEvent event) {
        lotRepository.insert(
                event.userId(),
                event.symbol(),
                event.tradeId(),
                event.quantity(),
                event.price(),
                Timestamp.from(event.executedAt())
        );
        log.info("Lote de compra creado [userId={}] [symbol={}] [qty={}] [price={}]",
                event.userId(), event.symbol(), event.quantity(), event.price());
    }


    ///FIFO
    /// -de donde sacar las acciones de que lote?
    private BigDecimal handleSell(TradeEvent event) {
        ///construye el purchaselot en base a el algoritmo fifo junto con las acciones de venta , cantidad etc.
        List<PurchaseLot> availableLots =
                lotRepository.findAvailableLotsFifoOrder(event.userId(), event.symbol());///TRAE LOTES EN ORDEN FIFO


        ///empieza de cero cantidad para vender y ganancias o perdidas
        BigDecimal quantityToSell = event.quantity();
        BigDecimal totalRealizedPnl = BigDecimal.ZERO;

        ///itera la colleccion purchase lot para crear availablelot
        for (PurchaseLot lot : availableLots) {

            ///comparamos la cantidad de acciones a vender y para hasta que se terminen las acciones que quedan por vender
            if (quantityToSell.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }


            /// Compara la cantidad disponible en este lote con la cantidad que falta vender
            BigDecimal quantityFromThisLot = lot.remainingQuantity().min(quantityToSell);
            /// min(...) devuelve el menor de los dos valores:
            ///- si el lote tiene menos de lo que falta vender, toma todo el lote
            ///- si el lote tiene más, toma solo lo necesario para completar la venta

            /**
             * event.price es el precio de cuanto vendiste la accion y saca el precio original del otro
             *substract metodo para restar la cantidad ganada y se multiplica por el numero de acciones vendidad
             *
             */
            BigDecimal pnlFromThisLot = event.price()
                    .subtract(lot.purchasePrice())
                    .multiply(quantityFromThisLot);
            /**
             * como p&l era 0 agregas el valor de la ganancia que es el resultado de .multiply(quantityFromThisLot
             */
            totalRealizedPnl = totalRealizedPnl.add(pnlFromThisLot);

            ///actualizamos el numero de acciones actuales
            BigDecimal newRemaining = lot.remainingQuantity().subtract(quantityFromThisLot);
            lotRepository.updateRemainingQuantity(lot.id(), newRemaining);

            log.debug("Lote [id={}] consumido: {} unidades, P&L parcial={}",
                    lot.id(), quantityFromThisLot, pnlFromThisLot);
            /**
            se resta de quantity el numero de acciones vendidas
             */
            quantityToSell = quantityToSell.subtract(quantityFromThisLot);
        }
        ///exepcion si el usuario esta vendiendo mas de lo que tiene
        if (quantityToSell.compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalStateException(
                    "Venta excede la posición disponible. Faltante: " + quantityToSell +
                            " para userId=" + event.userId() + " symbol=" + event.symbol());
        }

        log.info("Venta procesada con FIFO [userId={}] [symbol={}] [P&L realizado={}]",
                event.userId(), event.symbol(), totalRealizedPnl);

        return totalRealizedPnl;
    }

    /**
     * actualiza la posicion de los usuarios despues de movimientos de acciones
     */
    private Position recalculatePosition(Long userId, String symbol) {

        List<PurchaseLot> remainingLots =
                lotRepository.findAvailableLotsFifoOrder(userId, symbol); ///consulta FIFO de acciones

        BigDecimal totalQuantity = remainingLots.stream()
                .map(PurchaseLot::remainingQuantity) ///suma RemainingLot de los lotes tomando en cuenta userId y Symbol
                .reduce(BigDecimal.ZERO, BigDecimal::add); ///reduce significa “reduce todos los valores del stream a un único valor final”

        BigDecimal averageCost;///precio promedio de compras de sus acciones lo suma en un solo numero
            ///compara la cantidad de acciones con 0 si no lo son se crea un stream para sacar el promedio
        if (totalQuantity.compareTo(BigDecimal.ZERO) == 0) {
            averageCost = BigDecimal.ZERO;
        } else {
            /**
             * total cost stream de todos los lotes
             * multiplica la cantidad de acciones que quedan por la el precio de purchase y con reduce los convierte en un solo resultado
             */
            BigDecimal totalCost = remainingLots.stream()
                    .map(lot -> lot.remainingQuantity().multiply(lot.purchasePrice()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            averageCost = totalCost.divide(totalQuantity, 4, RoundingMode.HALF_UP);
        }
        ///al final se guarda en un upsert los datos si no existe insert si existe Update
        positionRepository.upsert(userId, symbol, totalQuantity, averageCost);

        ///retorna los nuevos valores
        return new Position(userId, symbol, totalQuantity, averageCost);
    }
}