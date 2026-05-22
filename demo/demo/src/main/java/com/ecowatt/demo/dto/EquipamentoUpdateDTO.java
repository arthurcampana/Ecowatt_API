package com.ecowatt.demo.dto;

import jakarta.validation.constraints.PositiveOrZero;

public class EquipamentoUpdateDTO {

    private String nome;
    private String modelo;
    @PositiveOrZero(message = "Consumo não pode ser negativo")
    private Double consumoPorHora;

    public EquipamentoUpdateDTO() {}

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public Double getConsumoPorHora() {
        return consumoPorHora;
    }

    public void setConsumoPorHora(Double consumoPorHora) {
        this.consumoPorHora = consumoPorHora;
    }
}
