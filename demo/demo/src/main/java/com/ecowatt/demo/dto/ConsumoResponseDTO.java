package com.ecowatt.demo.dto;

import com.ecowatt.demo.model.Consumo;

import java.time.LocalDateTime;

public record ConsumoResponseDTO(

        Long id,
        Long usuarioId,
        String nomeUsuario,
        Double consumoKwh,
        LocalDateTime dataRegistro

) {

    public ConsumoResponseDTO(Consumo consumo) {
        this(
                consumo.getId(),
                consumo.getUsuario().getId(),
                consumo.getUsuario().getNome(),
                consumo.getConsumoKwh(),
                consumo.getDataRegistro()
        );
    }
}