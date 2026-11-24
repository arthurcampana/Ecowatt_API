package com.ecowatt.demo.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usuario_id")
    private Long id;

    @Column(nullable = false)
    private String nome;
    @Column(nullable = false)
    private String senha;
    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "data_registro")
    private LocalDate dataRegistro;

    @Column(name = "role", nullable = false)
    private String role;

    // Consentimento LGPD: quando o usuario aceitou os termos e qual versao.
    @Column(name = "termos_aceitos_em", nullable = false)
    private LocalDateTime termosAceitosEm;

    @Column(name = "termos_versao")
    private String termosVersao;

    public Usuario() {}

    public Usuario(Long id, String nome, String senha, String email, LocalDate dataRegistro) {
        this(id, nome, senha, email, dataRegistro, "USER");
    }

    public Usuario(Long id, String nome, String senha, String email, LocalDate dataRegistro, String role) {
        this.id = id;
        this.nome = nome;
        this.senha = senha;
        this.email = email;
        this.dataRegistro = dataRegistro;
        this.role = role;
        // Consentimento registrado por padrao no momento da criacao; o cadastro
        // real sobrescreve com a versao vigente dos termos (ver UsuarioService).
        this.termosAceitosEm = LocalDateTime.now();
        this.termosVersao = "1.0";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getDataRegistro() {
        return dataRegistro;
    }

    public void setDataRegistro(LocalDate dataRegistro) {
        this.dataRegistro = dataRegistro;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getTermosAceitosEm() {
        return termosAceitosEm;
    }

    public void setTermosAceitosEm(LocalDateTime termosAceitosEm) {
        this.termosAceitosEm = termosAceitosEm;
    }

    public String getTermosVersao() {
        return termosVersao;
    }

    public void setTermosVersao(String termosVersao) {
        this.termosVersao = termosVersao;
    }
}