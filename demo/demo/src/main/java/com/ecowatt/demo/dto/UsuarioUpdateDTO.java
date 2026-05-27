package com.ecowatt.demo.dto;

import jakarta.validation.constraints.Size;

public record UsuarioUpdateDTO(

        String nome,

        @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
        String senha

) {}