package com.ecowatt.demo.integration.repository;

import com.ecowatt.demo.model.Equipamento;
import com.ecowatt.demo.repository.EquipamentoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class EquipamentoRepositoryIntegrationTest {

    @Autowired
    private EquipamentoRepository repository;

    @Test
    @DisplayName("findByNome deve localizar equipamento persistido")
    void findByNome_quandoExiste_deveRetornarEquipamento() {
        repository.saveAndFlush(new Equipamento(null, "Geladeira", "G1", 0.15));

        Equipamento encontrado = repository.findByNome("Geladeira").orElseThrow();

        assertThat(encontrado.getModelo()).isEqualTo("G1");
        assertThat(encontrado.getConsumoPorHora()).isEqualTo(0.15);
    }

    @Test
    @DisplayName("findByNome deve retornar vazio para nome inexistente")
    void findByNome_quandoInexistente_deveRetornarVazio() {
        assertThat(repository.findByNome("Inexistente")).isEmpty();
    }
}
