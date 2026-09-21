package com.ecowatt.demo.integration.service;

import com.ecowatt.demo.dto.EquipamentoRequestDTO;
import com.ecowatt.demo.dto.EquipamentoResponseDTO;
import com.ecowatt.demo.dto.EquipamentoUsuarioRequestDTO;
import com.ecowatt.demo.dto.EquipamentoUsuarioResponseDTO;
import com.ecowatt.demo.dto.EquipamentoUsuarioUpdateDTO;
import com.ecowatt.demo.dto.UsuarioRequestDTO;
import com.ecowatt.demo.dto.UsuarioResponseDTO;
import com.ecowatt.demo.service.EquipamentoService;
import com.ecowatt.demo.service.EquipamentoUsuarioService;
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
class EquipamentoUsuarioServiceIntegrationTest {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private EquipamentoService equipamentoService;

    @Autowired
    private EquipamentoUsuarioService vinculoService;

    @Test
    @DisplayName("INTEGRACAO: associar equipamento, recalcular horas, listar e excluir")
    void cicloCompleto_deveManterConsumoCalculado() {
        UsuarioResponseDTO usuario = usuarioService.cadastrarUsuario(new UsuarioRequestDTO(
                "Usuario Equip", "equip.integracao@exemplo.test", "senha123"));
        EquipamentoResponseDTO equipamento = equipamentoService.criar(
                new EquipamentoRequestDTO("Computador", "PC1", 1.5));
        EquipamentoUsuarioResponseDTO criado = vinculoService.cadastrarEquipamentoUsuario(
                new EquipamentoUsuarioRequestDTO("PC Sala", 2.0, null,
                        usuario.id(), equipamento.id()));

        EquipamentoUsuarioResponseDTO atualizado = vinculoService.atualizar(criado.id(),
                new EquipamentoUsuarioUpdateDTO(null, 4.0, null, null)).orElseThrow();

        assertThat(criado.consumoEsperado()).isEqualTo(3.0);
        assertThat(atualizado.consumoEsperado()).isEqualTo(6.0);
        assertThat(vinculoService.listar(usuario.id())).singleElement()
                .extracting(EquipamentoUsuarioResponseDTO::id).isEqualTo(criado.id());
        assertThat(vinculoService.deletar(criado.id())).isTrue();
        assertThat(vinculoService.buscarPorId(criado.id())).isEmpty();
    }
}
