package com.ecowatt.demo.integration.service;

import com.ecowatt.demo.dto.EquipamentoRequestDTO;
import com.ecowatt.demo.dto.EquipamentoResponseDTO;
import com.ecowatt.demo.dto.EquipamentoUpdateDTO;
import com.ecowatt.demo.service.EquipamentoService;
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
class EquipamentoServiceIntegrationTest {

    @Autowired
    private EquipamentoService service;

    @Test
    @DisplayName("INTEGRACAO: criar, atualizar, listar e excluir equipamento")
    void cicloCompleto_devePersistirAlteracoes() {
        EquipamentoResponseDTO criado = service.criar(
                new EquipamentoRequestDTO("Geladeira", "G1", 0.15));

        EquipamentoResponseDTO atualizado = service.atualizar(criado.id(),
                new EquipamentoUpdateDTO("Geladeira Nova", null, 0.12)).orElseThrow();

        assertThat(atualizado.nome()).isEqualTo("Geladeira Nova");
        assertThat(atualizado.modelo()).isEqualTo("G1");
        assertThat(atualizado.consumoPorHora()).isEqualTo(0.12);
        assertThat(service.listar()).extracting(EquipamentoResponseDTO::id)
                .contains(criado.id());
        assertThat(service.deletar(criado.id())).isTrue();
        assertThat(service.buscar(criado.id())).isEmpty();
    }
}
