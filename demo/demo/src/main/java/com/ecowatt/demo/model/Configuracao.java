package com.ecowatt.demo.model;

import com.ecowatt.demo.model.Cliente;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "configuracao")
public class Configuracao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    private Double valorTarifa;
    private Double meta;

    private LocalDate dataFechamento;


    private String unidadeMedida;

    public Configuracao() {}

    public Configuracao(Long id, Cliente cliente, Double valorTarifa,
                        Double meta, LocalDate dataFechamento, String unidadeMedida) {
        this.id = id;
        this.cliente = cliente;
        this.valorTarifa = valorTarifa;
        this.meta = meta;
        this.dataFechamento = dataFechamento;
        this.unidadeMedida = unidadeMedida;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public Double getValorTarifa() { return valorTarifa; }
    public void setValorTarifa(Double valorTarifa) { this.valorTarifa = valorTarifa; }

    public Double getMeta() { return meta; }
    public void setMeta(Double meta) { this.meta = meta; }

    public LocalDate getDataFechamento() { return dataFechamento; }
    public void setDataFechamento(LocalDate dataFechamento) { this.dataFechamento = dataFechamento; }

    public String getUnidadeMedida() { return unidadeMedida; }
    public void setUnidadeMedida(String unidadeMedida) { this.unidadeMedida = unidadeMedida; }
}