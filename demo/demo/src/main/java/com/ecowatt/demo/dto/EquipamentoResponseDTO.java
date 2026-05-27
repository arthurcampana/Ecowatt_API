package com.ecowatt.demo.dto;

import com.ecowatt.demo.model.Equipamento;

public record EquipamentoResponseDTO(

        Long id,
        String nome,
        String modelo,
        Double consumoPorHora

) {

    public EquipamentoResponseDTO(Equipamento equipamento) {
        this(
                equipamento.getId(),
                equipamento.getNome(),
                equipamento.getModelo(),
                equipamento.getConsumoPorHora()
        );
    }
}