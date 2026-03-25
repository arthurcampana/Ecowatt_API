package com.ecowatt.demo.model;

import java.time.LocalDateTime;

public class Cliente {
    private Long cliente_id;
    private String nome;
    private String senha;
    private String email;
    private LocalDateTime data_registro;

    public Cliente(Long cliente, String nome, String senha, String email, LocalDateTime data_registro) {
        this.cliente_id = cliente;
        this.nome = nome;
        this.senha = senha;
        this.email = email;
        this.data_registro = data_registro;

    }

    public Long getCliente_id() {
        return cliente_id;
    }

    public void setCliente_id(Long cliente_id) {
        this.cliente_id = cliente_id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }


}