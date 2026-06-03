package com.ecowatt.demo.dto;

import com.ecowatt.demo.model.Configuracao;

import java.math.BigDecimal;

public record ConfiguracaoResponseDTO(

        Long id,
        Long usuarioid,
        BigDecimal valorTarifa,
        Double meta,
        String unidadeMedida
) {
    public ConfiguracaoResponseDTO(Configuracao config) {
        this(
                config.getId(),
                config.getCliente().getId(),
                config.getValorTarifa(),
                config.getMeta(),
                config.getUnidadeMedida()

                );

    }

}


