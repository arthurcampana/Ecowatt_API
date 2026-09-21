package com.ecowatt.demo.integration.service;

import com.ecowatt.demo.dto.LoginDTO;
import com.ecowatt.demo.dto.LoginResponseDTO;
import com.ecowatt.demo.dto.UsuarioRequestDTO;
import com.ecowatt.demo.service.AuthenticationService;
import com.ecowatt.demo.service.JwtService;
import com.ecowatt.demo.service.UsuarioService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthenticationServiceIntegrationTest {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private JwtService jwtService;

    @Test
    @DisplayName("INTEGRACAO: AuthenticationManager, UserDetails e JWT devem autenticar usuario real")
    void autenticar_quandoCredenciaisValidas_deveGerarJwtValido() {
        usuarioService.cadastrarUsuario(new UsuarioRequestDTO(
                "Usuario Auth", "auth.integracao@exemplo.test", "senha123"));

        LoginResponseDTO resposta = authenticationService.autenticar(
                new LoginDTO("auth.integracao@exemplo.test", "senha123"));
        String token = resposta.token().substring("Bearer ".length());

        assertThat(resposta.email()).isEqualTo("auth.integracao@exemplo.test");
        assertThat(jwtService.validarToken(token)).isEqualTo("auth.integracao@exemplo.test");
    }

    @Test
    @DisplayName("INTEGRACAO: senha incorreta deve ser rejeitada")
    void autenticar_quandoSenhaIncorreta_deveFalhar() {
        usuarioService.cadastrarUsuario(new UsuarioRequestDTO(
                "Usuario Auth 2", "auth.erro@exemplo.test", "senha123"));

        assertThatThrownBy(() -> authenticationService.autenticar(
                new LoginDTO("auth.erro@exemplo.test", "incorreta")))
                .isInstanceOf(BadCredentialsException.class);
    }
}
