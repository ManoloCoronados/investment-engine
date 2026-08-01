package com.investmentengine.reporting.controller;

import com.investmentengine.reporting.model.PnlView;
import com.investmentengine.reporting.model.PositionView;
import com.investmentengine.reporting.model.TradeView;
import com.investmentengine.reporting.repository.PnlViewRepository;
import com.investmentengine.reporting.repository.PositionViewRepository;
import com.investmentengine.reporting.repository.TradeViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController

public class ReportController {
    private final TradeViewRepository tradeRepository;
    private final PositionViewRepository positionRepository;
    private final PnlViewRepository pnlRepository;

    @GetMapping("/api/v1/reports/trades/{userId}")
    public List<TradeView> getTrades(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,///numero de paginas para calcular
            @RequestParam(defaultValue = "20") int size) { ///cantidad de filas limit   offset size * page
        return tradeRepository.findByUserId(userId, page, size);
    }

    @GetMapping("/api/v1/reports/portfolio/{userId}")
    public List<PositionView> getPortfolio( @PathVariable Long userId) {
        return positionRepository.findByUserId(userId);
    }



    @GetMapping("/api/v1/reports/pnl/{userId}")
    public List<PnlView> getPnl(@PathVariable Long userId){
        return pnlRepository.findByUserId(userId);
    }






}

