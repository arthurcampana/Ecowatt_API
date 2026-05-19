package com.ecowatt.demo.dto;

import java.time.LocalDateTime;

public class ConsumoRequestDTO {

    private Long usuarioId;
    private Double consumoKwh;
    private LocalDateTime dataRegistro;

    public ConsumoRequestDTO(Long usuarioId,
                             Double consumoKwh,
                             LocalDateTime dataRegistro) {

        this.usuarioId = usuarioId;
        this.consumoKwh = consumoKwh;
        this.dataRegistro = dataRegistro;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public Double getConsumoKwh() {
        return consumoKwh;
    }

    public LocalDateTime getDataRegistro() {
        return dataRegistro;
    }
}