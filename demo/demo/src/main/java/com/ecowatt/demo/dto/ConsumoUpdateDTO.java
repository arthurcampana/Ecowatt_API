package com.ecowatt.demo.dto;

import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDateTime;

public record ConsumoUpdateDTO(

        @PositiveOrZero(message = "Consumo não pode ser negativo")
        Double consumoKwh,

        LocalDateTime dataRegistro

) {}