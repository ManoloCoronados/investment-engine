package com.investmentengine.pnl.service;

import com.investmentengine.pnl.model.*;
import com.investmentengine.pnl.producer.PnlCalculatedProducer;
import com.investmentengine.pnl.repository.LatestPriceRepository;
import com.investmentengine.pnl.repository.PositionSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PnlCalculationService {

    private final PositionSnapshotRepository positionRepository;
    private final LatestPriceRepository priceRepository;
    private final PnlCalculatedProducer producer;

    /**
     *
     *Actualizamos Nuestro Snapshot de positions
     */
    public void onPortfolioUpdated(PortfolioUpdatedEvent event) {

        positionRepository.upsert(
                event.userId(), event.symbol(),
                event.newTotalQuantity(), event.newAverageCost(),
                event.updatedAt()
        );
        /**
         * si los precios son nullos no hay actualizaciones entonces se espera
         */
        Optional<LatestPrice> latestPrice = priceRepository.findBySymbol(event.symbol());

        if (latestPrice.isEmpty()) {

            log.debug("No hay precio conocido para [symbol={}], esperando prices.updated",
                    event.symbol());
            return;
        }
        /**
         * si realmente hay posiciones se actualizan los campos
         */
        calculateAndPublish(event.userId(), event.symbol(),
                event.newTotalQuantity(), event.newAverageCost(), latestPrice.get().price());
    }

    /**
     * metodo para actualizar
     */
    public void onPriceUpdated(PriceUpdatedEvent event) {

        ///actualizamos el snapshot
        priceRepository.upsert(event.symbol(), event.price(), event.updatedAt());

        ///iteramos y buscamos usuarios afectados
        List<PositionSnapshot> affectedPositions =
                positionRepository.findAllBySymbol(event.symbol());

        if (affectedPositions.isEmpty()) {
            log.debug("Ningún usuario tiene posición en [symbol={}]", event.symbol());
            return;
        }

        ///recalculamos las posiciones con nuevos valores de cada usuario afectado
        for (PositionSnapshot position : affectedPositions) {
            calculateAndPublish(
                    position.userId(), position.symbol(),
                    position.totalQuantity(), position.averageCost(), event.price()
            );
        }
    }

    ///metodo para calcular y publicar
    private void calculateAndPublish(Long userId, String symbol,
                                     BigDecimal totalQuantity, BigDecimal averageCost,
                                     BigDecimal currentPrice) {
        ///si es 0 no hay nada que calcular en PHL
        if (totalQuantity.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }

        /**
         * (precio actual - costo promedio ) x cantidad de acciones
         */
        BigDecimal unrealizedPnl = currentPrice
                .subtract(averageCost)
                .multiply(totalQuantity);

        /**
         * creamos el evento en base a los nuevos valores y lo publicamos en producer
         */
        PnlCalculatedEvent event = new PnlCalculatedEvent(
                userId, symbol, unrealizedPnl, currentPrice,
                totalQuantity, averageCost, Instant.now()
        );

        producer.publish(event);

        log.info("P&L no realizado calculado [userId={}] [symbol={}] [P&L={}]",
                userId, symbol, unrealizedPnl);
    }
}