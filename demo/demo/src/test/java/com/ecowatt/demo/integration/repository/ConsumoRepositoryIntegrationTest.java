package com.ecowatt.demo.integration.repository;

import com.ecowatt.demo.model.Consumo;
import com.ecowatt.demo.model.Usuario;
import com.ecowatt.demo.repository.ConsumoRepository;
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
class ConsumoRepositoryIntegrationTest {

    @Autowired
    private ConsumoRepository consumoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Usuario usuario(String nome, String email) {
        return usuarioRepository.save(new Usuario(null, nome, "hash", email, LocalDate.now()));
    }

    @Test
    @DisplayName("findByUsuarioId deve isolar os consumos de cada usuario")
    void findByUsuarioId_deveRetornarSomenteConsumosDoUsuario() {
        Usuario a = usuario("A", "a.consumo@exemplo.test");
        Usuario b = usuario("B", "b.consumo@exemplo.test");
        consumoRepository.save(new Consumo(null, a, LocalDateTime.now(), 10.0));
        consumoRepository.save(new Consumo(null, a, LocalDateTime.now(), 20.0));
        consumoRepository.save(new Consumo(null, b, LocalDateTime.now(), 99.0));
        consumoRepository.flush();

        List<Consumo> encontrados = consumoRepository.findByUsuarioId(a.getId());

        assertThat(encontrados).hasSize(2);
        assertThat(encontrados).allMatch(c -> c.getUsuario().getId().equals(a.getId()));
        assertThat(encontrados).extracting(Consumo::getConsumoKwh)
                .containsExactlyInAnyOrder(10.0, 20.0);
    }

    @Test
    @DisplayName("findByUsuarioId deve retornar lista vazia sem registros")
    void findByUsuarioId_quandoSemConsumos_deveRetornarVazio() {
        Usuario usuario = usuario("Sem Consumo", "sem.consumo@exemplo.test");

        assertThat(consumoRepository.findByUsuarioId(usuario.getId())).isEmpty();
    }
}
