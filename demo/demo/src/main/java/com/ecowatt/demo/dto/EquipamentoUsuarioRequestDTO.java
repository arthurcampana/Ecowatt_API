package com.ecowatt.demo.dto;

public class EquipamentoUsuarioRequestDTO {
    private String nomeIdentificacao;
    private double HorasPorDia;
    private double ConsumoEsperado;
    private Long usuarioId;
    private Long equipamentoId;

    public EquipamentoUsuarioRequestDTO(String nomeIdentificacao, double consumoEsperado, double horasPorDia, Long equipamentoId, Long usuarioId) {
        this.nomeIdentificacao = nomeIdentificacao;
        ConsumoEsperado = consumoEsperado;
        HorasPorDia = horasPorDia;
        this.equipamentoId = equipamentoId;
        this.usuarioId = usuarioId;
    }

    public String getNomeIdentificacao() {
        return nomeIdentificacao;
    }

    public Long getEquipamentoId() {
        return equipamentoId;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public double getConsumoEsperado() {
        return ConsumoEsperado;
    }

    public double getHorasPorDia() {
        return HorasPorDia;
    }
}
