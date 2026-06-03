package com.ecowatt.demo.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record ConfiguracaoUpdateDTO(

        @NotNull(message = "Valor obrigatório")
        @PositiveOrZero(message = "Valor da tarifa não pode ser negativo")
        BigDecimal valorTarifa,

        @NotNull(message = "Valor obrigatório")
        @PositiveOrZero(message = "Valor da meta não pode ser negativo")
        Double meta,

        @NotNull(message = "Valor obrigatório")
        String unidadeMedida

) {}
