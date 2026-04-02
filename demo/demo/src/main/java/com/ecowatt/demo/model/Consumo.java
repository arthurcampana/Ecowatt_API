package com.ecowatt.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "consumo")
public class Consumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "data_registro")
    private LocalDateTime dataRegistro;

    @Column(nullable = false)
    private Double consumoKwh;

    public Consumo() {}

    public Consumo(Long id, Usuario usuario, LocalDateTime dataRegistro, Double consumoKwh) {
        this.id = id;
        this.usuario = usuario;
        this.dataRegistro = dataRegistro;
        this.consumoKwh = consumoKwh;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public LocalDateTime getDataRegistro() { return dataRegistro; }
    public void setDataRegistro(LocalDateTime dataRegistro) { this.dataRegistro = dataRegistro; }

    public Double getConsumoKwh() { return consumoKwh; }
    public void setConsumoKwh(Double consumoKwh) { this.consumoKwh = consumoKwh; }
}