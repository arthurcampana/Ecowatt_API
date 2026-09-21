package com.ecowatt.demo.integration.service;

import com.ecowatt.demo.dto.ConfiguracaoRequestDTO;
import com.ecowatt.demo.dto.ConfiguracaoResponseDTO;
import com.ecowatt.demo.dto.ConfiguracaoUpdateDTO;
import com.ecowatt.demo.dto.UsuarioRequestDTO;
import com.ecowatt.demo.dto.UsuarioResponseDTO;
import com.ecowatt.demo.service.ConfiguracaoService;
import com.ecowatt.demo.service.UsuarioService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ConfiguracaoServiceIntegrationTest {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ConfiguracaoService configuracaoService;

    @Test
    @DisplayName("INTEGRACAO: salvar, buscar, alterar e excluir configuracao")
    void cicloCompleto_devePersistirConfiguracao() {
        UsuarioResponseDTO usuario = usuarioService.cadastrarUsuario(new UsuarioRequestDTO(
                "Usuario Config", "config.integracao@exemplo.test", "senha123"));
        ConfiguracaoResponseDTO criada = configuracaoService.salvar(new ConfiguracaoRequestDTO(
                usuario.id(), new BigDecimal("0.85"), 300.0, "kWh"));

        ConfiguracaoResponseDTO atualizada = configuracaoService.alterarConfig(criada.id(),
                new ConfiguracaoUpdateDTO(new BigDecimal("0.95"), 280.0, "kWh"))
                .orElseThrow();

        assertThat(configuracaoService.buscarPorUsuario(usuario.id())).isPresent();
        assertThat(atualizada.valorTarifa()).isEqualByComparingTo("0.95");
        assertThat(atualizada.meta()).isEqualTo(280.0);
        assertThat(configuracaoService.excluirConfig(criada.id())).isTrue();
        assertThat(configuracaoService.buscarConfig(criada.id())).isEmpty();
    }
}
