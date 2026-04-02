package com.ecowatt.demo.model;

public class Equipamento {

    private Long id_equipamento;
    private String nome;
    private String modelo;
    private Double consumoPorHora;

    public Equipamento(Long id_equipamento, String nome, String modelo, Double consumoPorHora) {
        this.id_equipamento = id_equipamento;
        this.nome = nome;
        this.modelo = modelo;
        this.consumoPorHora = consumoPorHora;
    }

    public Long getId_equipamento() { return id_equipamento; }
    public void setId_equipamento(Long id_equipamento) { this.id_equipamento = id_equipamento; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public Double getConsumoPorHora() { return consumoPorHora; }
    public void setConsumoPorHora(Double consumoPorHora) { this.consumoPorHora = consumoPorHora; }
}