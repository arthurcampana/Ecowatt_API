package com.ecowatt.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "previsao_consumo")
public class PrevisaoConsumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "ano_mes", length = 7)
    private String anoMes;

    @Column(name = "consumo_previsto")
    private Double consumoPrevisto;

    @Column(name = "gerado_em")
    private LocalDateTime geradoEm;

    public PrevisaoConsumo() {}

    public PrevisaoConsumo(Long id, Usuario usuario, String anoMes, Double consumoPrevisto, LocalDateTime geradoEm) {
        this.id = id;
        this.usuario = usuario;
        this.anoMes = anoMes;
        this.consumoPrevisto = consumoPrevisto;
        this.geradoEm = geradoEm;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public String getAnoMes() { return anoMes; }
    public void setAnoMes(String anoMes) { this.anoMes = anoMes; }

    public Double getConsumoPrevisto() { return consumoPrevisto; }
    public void setConsumoPrevisto(Double consumoPrevisto) { this.consumoPrevisto = consumoPrevisto; }

    public LocalDateTime getGeradoEm() { return geradoEm; }
    public void setGeradoEm(LocalDateTime geradoEm) { this.geradoEm = geradoEm; }
}
