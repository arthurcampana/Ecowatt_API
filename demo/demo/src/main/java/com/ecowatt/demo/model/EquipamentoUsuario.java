package com.ecowatt.demo.model;

import jakarta.persistence.*;

@Entity
@Table
public class EquipamentoUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "equipamentoUsuario_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "equipamento_id")
    private Equipamento equipamento;

    private String nomeIdentificacao;
    private double HorasPorDia;
    private double ConsumoEsperado;

    public double getConsumoEsperado() {
        return ConsumoEsperado;
    }

    public void setConsumoEsperado(double consumoEsperado) {
        ConsumoEsperado = consumoEsperado;
    }

    public double getHorasPorDia() {
        return HorasPorDia;
    }

    public void setHorasPorDia(double horasPorDia) {
        HorasPorDia = horasPorDia;
    }

    public String getNomeIdentificacao() {
        return nomeIdentificacao;
    }

    public void setNomeIdentificacao(String nomeIdentificacao) {
        this.nomeIdentificacao = nomeIdentificacao;
    }

    public Equipamento getEquipamento() {
        return equipamento;
    }

    public void setEquipamento(Equipamento equipamento) {
        this.equipamento = equipamento;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
