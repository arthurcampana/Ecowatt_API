package com.ecowatt.demo.dto;

import com.ecowatt.demo.model.EquipamentoUsuario;

public record EquipamentoUsuarioResponseDTO(

        Long id,
        String nomeIdentificacao,

        Double horasPorDia,
        Double consumoEsperado,

        Long usuarioId,
        String nomeUsuario,

        Long equipamentoId,
        String nomeEquipamento

) {

    public EquipamentoUsuarioResponseDTO(
            EquipamentoUsuario equipUser
    ) {

        this(
                equipUser.getId(),

                equipUser.getNomeIdentificacao(),

                equipUser.getHorasPorDia(),

                equipUser.getConsumoEsperado(),

                equipUser.getUsuario().getId(),

                equipUser.getUsuario().getNome(),

                equipUser.getEquipamento().getId(),

                equipUser.getEquipamento().getNome()
        );
    }
}