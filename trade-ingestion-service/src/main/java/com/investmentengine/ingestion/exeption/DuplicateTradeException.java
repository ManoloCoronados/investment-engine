package com.investmentengine.ingestion.exeption;

public class DuplicateTradeException extends RuntimeException{

    public DuplicateTradeException(String tradeId) {
        super("Trade ya procesado, tradeId=" + tradeId);
    }
    }


