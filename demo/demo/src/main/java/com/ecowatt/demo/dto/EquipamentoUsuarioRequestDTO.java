package com.ecowatt.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record EquipamentoUsuarioRequestDTO(

        @NotBlank(message = "Nome obrigatório")
        String nomeIdentificacao,

        @PositiveOrZero(message = "Horas não podem ser negativas")
        Double horasPorDia,

        @PositiveOrZero(message = "Consumo não pode ser negativo")
        Double consumoEsperado,

        @NotNull(message = "Usuário obrigatório")
        Long usuarioId,

        @NotNull(message = "Equipamento obrigatório")
        Long equipamentoId

) {}