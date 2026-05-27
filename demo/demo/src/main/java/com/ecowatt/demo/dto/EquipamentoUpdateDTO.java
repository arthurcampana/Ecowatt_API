package com.ecowatt.demo.dto;

import jakarta.validation.constraints.PositiveOrZero;

public record EquipamentoUpdateDTO(

        String nome,

        String modelo,

        @PositiveOrZero(message = "Consumo não pode ser negativo")
        Double consumoPorHora

) {}