package com.ecowatt.demo.integration.service;

import com.ecowatt.demo.dto.ConsumoRequestDTO;
import com.ecowatt.demo.dto.ConsumoResponseDTO;
import com.ecowatt.demo.dto.UsuarioRequestDTO;
import com.ecowatt.demo.dto.UsuarioResponseDTO;
import com.ecowatt.demo.service.ConsumoService;
import com.ecowatt.demo.service.UsuarioService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ConsumoServiceIntegrationTest {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ConsumoService consumoService;

    @Test
    @DisplayName("INTEGRACAO: salvar e listar consumos persistidos por usuario")
    void salvarEListar_quandoUsuarioExiste_deveRetornarSomenteSeusConsumos() {
        UsuarioResponseDTO usuario = usuarioService.cadastrarUsuario(
                new UsuarioRequestDTO("Usuario Consumo", "consumo.usuario@exemplo.test", "senha123"));
        LocalDateTime data = LocalDateTime.of(2026, 3, 15, 10, 30);

        ConsumoResponseDTO criado = consumoService.salvar(
                new ConsumoRequestDTO(usuario.id(), 125.75, data));
        List<ConsumoResponseDTO> consumos = consumoService.listarPorUsuario(usuario.id());

        assertThat(criado.id()).isNotNull();
        assertThat(consumos).hasSize(1);
        assertThat(consumos.get(0).usuarioId()).isEqualTo(usuario.id());
        assertThat(consumos.get(0).consumoKwh()).isEqualTo(125.75);
        assertThat(consumos.get(0).dataRegistro()).isEqualTo(data);
    }
}
