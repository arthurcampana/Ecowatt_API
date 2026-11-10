package com.ecowatt.demo.dto;

import com.ecowatt.demo.model.PrevisaoConsumo;

import java.time.LocalDateTime;

public record PrevisaoConsumoDTO(

        Long usuarioId,
        String anoMes,
        Double consumoPrevisto,
        LocalDateTime geradoEm

) {

    public PrevisaoConsumoDTO(PrevisaoConsumo previsao) {
        this(
                previsao.getUsuario().getId(),
                previsao.getAnoMes(),
                previsao.getConsumoPrevisto(),
                previsao.getGeradoEm()
        );
    }
}
