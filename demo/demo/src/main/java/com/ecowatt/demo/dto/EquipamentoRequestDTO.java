package com.ecowatt.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record EquipamentoRequestDTO(

        @NotBlank(message = "Nome obrigatório")
        String nome,

        @NotBlank(message = "Modelo obrigatório")
        String modelo,

        @NotNull(message = "Consumo obrigatório")
        @PositiveOrZero(message = "Consumo não pode ser negativo")
        Double consumoPorHora

) {}