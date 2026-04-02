package com.ecowatt.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "equipamento")
public class Equipamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "equipamento_id")
    private Long id;

    private String nome;
    private String modelo;
    private Double consumoPorHora;

    public Equipamento() {}

    public Equipamento(Long id, String nome, String modelo, Double consumoPorHora) {
        this.id = id;
        this.nome = nome;
        this.modelo = modelo;
        this.consumoPorHora = consumoPorHora;
    }

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }

    public void setNome(String nome) { this.nome = nome; }

    public String getModelo() { return modelo; }

    public void setModelo(String modelo) { this.modelo = modelo; }

    public Double getConsumoPorHora() { return consumoPorHora; }

    public void setConsumoPorHora(Double consumoPorHora) { this.consumoPorHora = consumoPorHora; }
}