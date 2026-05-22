package com.ecowatt.demo.dto;

public class EquipamentoRequestDTO {

    private String nome;
    private String modelo;
    private Double consumoPorHora;

    public EquipamentoRequestDTO(String nome, String modelo, Double consumoPorHora) {
        this.nome = nome;
        this.modelo = modelo;
        this.consumoPorHora = consumoPorHora;
    }

    public String getModelo() {
        return modelo;
    }

    public String getNome() {
        return nome;
    }

    public Double getConsumoPorHora() {
        return consumoPorHora;
    }
}
