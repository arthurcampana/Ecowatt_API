package com.ecowatt.demo.integration.service;

import com.ecowatt.demo.dto.LoginDTO;
import com.ecowatt.demo.dto.LoginResponseDTO;
import com.ecowatt.demo.dto.UsuarioRequestDTO;
import com.ecowatt.demo.dto.UsuarioResponseDTO;
import com.ecowatt.demo.repository.UsuarioRepository;
import com.ecowatt.demo.service.UsuarioService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UsuarioServiceIntegrationTest {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    @DisplayName("INTEGRACAO: cadastrar e autenticar usuario usando banco H2")
    void cadastrarELogin_quandoDadosValidos_devePersistirEAutenticar() {
        UsuarioRequestDTO cadastro = new UsuarioRequestDTO(
                "Usuario Integracao", "integracao.usuario@exemplo.test", "senha123");

        UsuarioResponseDTO criado = usuarioService.cadastrarUsuario(cadastro);
        LoginResponseDTO login = usuarioService.login(
                new LoginDTO("integracao.usuario@exemplo.test", "senha123"));

        assertThat(criado.id()).isNotNull();
        assertThat(usuarioRepository.findByEmail("integracao.usuario@exemplo.test")).isPresent();
        assertThat(login.token()).startsWith("Bearer ");
        assertThat(login.email()).isEqualTo("integracao.usuario@exemplo.test");
    }
}
