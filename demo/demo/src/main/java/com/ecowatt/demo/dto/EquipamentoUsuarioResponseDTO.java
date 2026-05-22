package com.ecowatt.demo.dto;

import com.ecowatt.demo.model.EquipamentoUsuario;

public class EquipamentoUsuarioResponseDTO {
    private Long id;
    private String nomeIdentificacao;
    private double HorasPorDia;
    private double ConsumoEsperado;
    private Long usuarioId;
    private Long equipamentoId;

    public EquipamentoUsuarioResponseDTO(EquipamentoUsuario equipUser) {
        HorasPorDia = equipUser.getHorasPorDia();
        this.id = equipUser.getId();
        this.nomeIdentificacao = equipUser.getNomeIdentificacao();
        ConsumoEsperado = equipUser.getConsumoEsperado();
        this.usuarioId = equipUser.getUsuario().getId();
        this.equipamentoId = equipUser.getEquipamento().getId();
    }

    public Long getId() {
        return id;
    }

    public String getNomeIdentificacao() {
        return nomeIdentificacao;
    }

    public double getHorasPorDia() {
        return HorasPorDia;
    }

    public double getConsumoEsperado() {
        return ConsumoEsperado;
    }

    public Long getEquipamentoId() {
        return equipamentoId;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }
}
