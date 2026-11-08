package com.fiap.mechanical_hub.repair_service.application.gateways;

public record StockReservationResult(boolean reserved, String motivo) {

    public static StockReservationResult success() {
        return new StockReservationResult(true, null);
    }

    public static StockReservationResult failure(String motivo) {
        return new StockReservationResult(false, motivo);
    }
}
