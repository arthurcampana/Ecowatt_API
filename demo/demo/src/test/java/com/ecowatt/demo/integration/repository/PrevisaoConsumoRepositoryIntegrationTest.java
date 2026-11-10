package com.ecowatt.demo.integration.repository;

import com.ecowatt.demo.model.PrevisaoConsumo;
import com.ecowatt.demo.model.Usuario;
import com.ecowatt.demo.repository.PrevisaoConsumoRepository;
import com.ecowatt.demo.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class PrevisaoConsumoRepositoryIntegrationTest {

    @Autowired
    private PrevisaoConsumoRepository previsaoConsumoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Usuario usuario(String nome, String email) {
        return usuarioRepository.save(new Usuario(null, nome, "hash", email, LocalDate.now()));
    }

    @Test
    @DisplayName("findByUsuarioIdOrderByAnoMes deve isolar por usuario e ordenar por ano_mes")
    void findByUsuarioIdOrderByAnoMes_deveIsolarEOrdenar() {
        Usuario a = usuario("A", "a.previsao@exemplo.test");
        Usuario b = usuario("B", "b.previsao@exemplo.test");

        LocalDateTime agora = LocalDateTime.now();
        // Inseridas fora de ordem de propósito, para provar a ordenacao.
        previsaoConsumoRepository.save(new PrevisaoConsumo(null, a, "2025-03", 30.0, agora));
        previsaoConsumoRepository.save(new PrevisaoConsumo(null, a, "2025-01", 10.0, agora));
        previsaoConsumoRepository.save(new PrevisaoConsumo(null, a, "2025-02", 20.0, agora));
        previsaoConsumoRepository.save(new PrevisaoConsumo(null, b, "2025-01", 99.0, agora));
        previsaoConsumoRepository.flush();

        List<PrevisaoConsumo> encontrados =
                previsaoConsumoRepository.findByUsuarioIdOrderByAnoMes(a.getId());

        assertThat(encontrados).hasSize(3);
        assertThat(encontrados).allMatch(p -> p.getUsuario().getId().equals(a.getId()));
        assertThat(encontrados).extracting(PrevisaoConsumo::getAnoMes)
                .containsExactly("2025-01", "2025-02", "2025-03");
    }

    @Test
    @DisplayName("findByUsuarioIdOrderByAnoMes deve retornar lista vazia sem registros")
    void findByUsuarioIdOrderByAnoMes_quandoSemPrevisoes_deveRetornarVazio() {
        Usuario usuario = usuario("Sem Previsao", "sem.previsao@exemplo.test");

        assertThat(previsaoConsumoRepository.findByUsuarioIdOrderByAnoMes(usuario.getId())).isEmpty();
    }
}
