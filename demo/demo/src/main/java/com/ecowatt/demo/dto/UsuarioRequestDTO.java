package com.ecowatt.demo.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioRequestDTO(
        @NotBlank(message = "Nome obrigatório")
        String nome,

        @Email(message = "Email inválido")
        @NotBlank(message = "Email obrigatório")
        String email,

        @NotBlank(message = "Senha obrigatória")
        @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
        String senha,

        // Consentimento LGPD: o cadastro só é aceito com este campo verdadeiro.
        // @NotNull barra o campo ausente (um Boolean nulo passaria pelo @AssertTrue);
        // @AssertTrue barra o valor false.
        @NotNull(message = "É necessário aceitar os Termos de Uso e a Política de Privacidade")
        @AssertTrue(message = "É necessário aceitar os Termos de Uso e a Política de Privacidade")
        Boolean aceiteTermos
) {}
