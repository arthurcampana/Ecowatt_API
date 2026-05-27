package com.ecowatt.demo.dto;

import com.ecowatt.demo.model.Usuario;

public record UsuarioResponseDTO(

        Long id,
        String nome,
        String email

) {

    public UsuarioResponseDTO(Usuario usuario) {
        this(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail()
        );
    }
}