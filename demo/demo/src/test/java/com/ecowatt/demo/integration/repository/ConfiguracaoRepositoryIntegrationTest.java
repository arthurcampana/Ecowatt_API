package com.ecowatt.demo.integration.repository;

import com.ecowatt.demo.model.Configuracao;
import com.ecowatt.demo.model.Usuario;
import com.ecowatt.demo.repository.ConfiguracaoRepository;
import com.ecowatt.demo.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ConfiguracaoRepositoryIntegrationTest {

    @Autowired
    private ConfiguracaoRepository configuracaoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    @DisplayName("findByUsuarioId deve retornar configuracao e preservar tarifa")
    void findByUsuarioId_quandoExiste_deveRetornarConfiguracao() {
        Usuario usuario = usuarioRepository.save(new Usuario(null, "Usuario", "hash",
                "config.repo@exemplo.test", LocalDate.now()));
        configuracaoRepository.saveAndFlush(new Configuracao(null, usuario,
                new BigDecimal("0.875"), 250.0, null, "kWh"));

        Configuracao encontrada = configuracaoRepository.findByUsuarioId(usuario.getId()).orElseThrow();

        assertThat(encontrada.getCliente().getId()).isEqualTo(usuario.getId());
        assertThat(encontrada.getValorTarifa()).isEqualByComparingTo("0.875");
        assertThat(encontrada.getMeta()).isEqualTo(250.0);
    }

    @Test
    @DisplayName("findByUsuarioId deve retornar vazio sem configuracao")
    void findByUsuarioId_quandoInexistente_deveRetornarVazio() {
        Usuario usuario = usuarioRepository.save(new Usuario(null, "Sem Config", "hash",
                "sem.config@exemplo.test", LocalDate.now()));

        assertThat(configuracaoRepository.findByUsuarioId(usuario.getId())).isEmpty();
    }
}
