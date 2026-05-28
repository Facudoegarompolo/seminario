package com.digitalqueue.model.enums;

public enum TipoDia {
    NORMAL(1.0),
    FERIADO(1.2),
    EVENTO(1.5),
    FIESTA(1.7),
    PICO_RED_SOCIAL(2.0);

    private final double multiplicador;

    TipoDia(double multiplicador) {
        this.multiplicador = multiplicador;
    }

    public double getMultiplicador() {
        return multiplicador;
    }
}
