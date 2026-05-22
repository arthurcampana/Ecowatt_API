package com.ecowatt.demo.dto;

import com.ecowatt.demo.model.Equipamento;

public class EquipamentoResponseDTO {

    private Long id;
    private String nome;
    private String modelo;
    private Double consumoPorHora;


    public EquipamentoResponseDTO(Equipamento equip) {
        this.id = equip.getId();
        this.nome = equip.getNome();
        this.modelo = equip.getModelo();
        this.consumoPorHora = equip.getConsumoPorHora();
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getModelo() {
        return modelo;
    }

    public Double getConsumoPorHora() {
        return consumoPorHora;
    }
}
