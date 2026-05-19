package com.ecowatt.demo.dto;

import com.ecowatt.demo.model.Consumo;

import java.time.LocalDateTime;

public class ConsumoResponseDTO {

    private Long id;
    private Long usuarioId;
    private String nomeUsuario;
    private Double consumoKwh;
    private LocalDateTime dataRegistro;

    public ConsumoResponseDTO(Consumo consumo) {

        this.id = consumo.getId();
        this.usuarioId = consumo.getUsuario().getId();
        this.nomeUsuario = consumo.getUsuario().getNome();
        this.consumoKwh = consumo.getConsumoKwh();
        this.dataRegistro = consumo.getDataRegistro();
    }

    public Long getId() {
        return id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public String getNomeUsuario() {
        return nomeUsuario;
    }

    public Double getConsumoKwh() {
        return consumoKwh;
    }

    public LocalDateTime getDataRegistro() {
        return dataRegistro;
    }
}