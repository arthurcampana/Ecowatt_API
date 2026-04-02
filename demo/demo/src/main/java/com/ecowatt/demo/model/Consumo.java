package com.ecowatt.demo.model;

import com.ecowatt.demo.model.Cliente;
import jakarta.persistence.*;

@Entity
@Table(name = "consumo")
public class Consumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_cliente")
    private Cliente cliente;

    private String periodo;

    private Double consumoRealKwh;
    private Double consumoEstimadoKwh;
    private Double diferenca;

    public Consumo() {}

    public Consumo(Long id, Cliente cliente, String periodo,
                   Double consumoRealKwh, Double consumoEstimadoKwh, Double diferenca) {
        this.id = id;
        this.cliente = cliente;
        this.periodo = periodo;
        this.consumoRealKwh = consumoRealKwh;
        this.consumoEstimadoKwh = consumoEstimadoKwh;
        this.diferenca = diferenca;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public String getPeriodo() { return periodo; }
    public void setPeriodo(String periodo) { this.periodo = periodo; }

    public Double getConsumoRealKwh() { return consumoRealKwh; }
    public void setConsumoRealKwh(Double consumoRealKwh) { this.consumoRealKwh = consumoRealKwh; }

    public Double getConsumoEstimadoKwh() { return consumoEstimadoKwh; }
    public void setConsumoEstimadoKwh(Double consumoEstimadoKwh) { this.consumoEstimadoKwh = consumoEstimadoKwh; }

    public Double getDiferenca() { return diferenca; }
    public void setDiferenca(Double diferenca) { this.diferenca = diferenca; }
}