package com.ecowatt.demo.dto;

import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDateTime;

public class ConsumoUpdateDTO {

    @PositiveOrZero(message = "Consumo não pode ser negativo")
    private Double consumoKwh;

    private LocalDateTime dataRegistro;

    public ConsumoUpdateDTO() {}

    public Double getConsumoKwh() {
        return consumoKwh;
    }

    public void setConsumoKwh(Double consumoKwh) {
        this.consumoKwh = consumoKwh;
    }

    public LocalDateTime getDataRegistro() {
        return dataRegistro;
    }

    public void setDataRegistro(LocalDateTime dataRegistro) {
        this.dataRegistro = dataRegistro;
    }
}