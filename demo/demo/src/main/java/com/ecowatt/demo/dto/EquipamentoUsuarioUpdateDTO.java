package com.ecowatt.demo.dto;

import jakarta.validation.constraints.PositiveOrZero;

public record EquipamentoUsuarioUpdateDTO(

        String nomeIdentificacao,

        @PositiveOrZero(message = "Horas não podem ser negativas")
        Double horasPorDia,

        @PositiveOrZero(message = "Consumo não pode ser negativo")
        Double consumoEsperado,

        Long equipamentoId

) {}