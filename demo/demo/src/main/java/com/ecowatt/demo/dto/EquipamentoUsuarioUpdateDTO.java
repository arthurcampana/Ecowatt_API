package com.ecowatt.demo.dto;

import jakarta.validation.constraints.PositiveOrZero;

public class EquipamentoUsuarioUpdateDTO {

    private String nomeIdentificacao;

    @PositiveOrZero(message = "Horas não podem ser negativas")
    private double HorasPorDia;

    @PositiveOrZero(message = "Consumo não pode ser negativo")
    private double ConsumoEsperado;

    public EquipamentoUsuarioUpdateDTO () {}

    public String getNomeIdentificacao() {
        return nomeIdentificacao;
    }

    public double getHorasPorDia() {
        return HorasPorDia;
    }

    public double getConsumoEsperado() {
        return ConsumoEsperado;
    }
}
