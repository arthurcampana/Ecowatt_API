package com.ecowatt.demo.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDateTime;

public record ConsumoRequestDTO(

        @NotNull(message = "Usuário obrigatório")
        Long usuarioId,

        @NotNull(message = "Consumo obrigatório")
        @PositiveOrZero(message = "Consumo não pode ser negativo")
        Double consumoKwh,

        LocalDateTime dataRegistro

) {}