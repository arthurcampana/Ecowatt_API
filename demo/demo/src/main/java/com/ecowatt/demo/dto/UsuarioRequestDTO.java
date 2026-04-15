package com.ecowatt.demo.dto;

import jakarta.validation.constraints.*;

public class UsuarioRequestDTO {

    @NotBlank
    private final String nome;

    @Email
    @NotBlank
    private final String email;

    @NotBlank
    @Size(min = 6)
    private final String senha;

    public UsuarioRequestDTO(String nome, String email, String senha) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
    }

    public String getNome() {
        return nome;
    }
    
    public String getEmail() {
        return email;
    }
    
    public String getSenha() {
        return senha;
    }
    
}