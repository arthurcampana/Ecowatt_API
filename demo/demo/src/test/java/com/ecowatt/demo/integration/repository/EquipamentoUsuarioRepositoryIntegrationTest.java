package com.ecowatt.demo.integration.repository;

import com.ecowatt.demo.model.Equipamento;
import com.ecowatt.demo.model.EquipamentoUsuario;
import com.ecowatt.demo.model.Usuario;
import com.ecowatt.demo.repository.EquipamentoRepository;
import com.ecowatt.demo.repository.EquipamentoUsuarioRepository;
import com.ecowatt.demo.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class EquipamentoUsuarioRepositoryIntegrationTest {

    @Autowired
    private EquipamentoUsuarioRepository vinculoRepository;

    @Autowired
    private EquipamentoRepository equipamentoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Usuario usuario(String email) {
        return usuarioRepository.save(new Usuario(null, "Usuario", "hash", email, LocalDate.now()));
    }

    private Equipamento equipamento(String nome) {
        return equipamentoRepository.save(new Equipamento(null, nome, "Modelo", 1.5));
    }

    private EquipamentoUsuario vinculo(Usuario usuario, Equipamento equipamento, String identificacao) {
        EquipamentoUsuario vinculo = new EquipamentoUsuario();
        vinculo.setUsuario(usuario);
        vinculo.setEquipamento(equipamento);
        vinculo.setNomeIdentificacao(identificacao);
        vinculo.setHorasPorDia(2.0);
        vinculo.setConsumoEsperado(3.0);
        return vinculoRepository.save(vinculo);
    }

    @Test
    @DisplayName("consultas devem localizar vinculo pelo usuario e equipamento")
    void consultas_quandoVinculoExiste_devemRetornarRegistro() {
        Usuario usuario = usuario("vinculo@exemplo.test");
        Equipamento equipamento = equipamento("Computador");
        EquipamentoUsuario salvo = vinculo(usuario, equipamento, "PC Sala");
        vinculoRepository.flush();

        assertThat(vinculoRepository.findAllByUsuarioId(usuario.getId()))
                .extracting(EquipamentoUsuario::getId).containsExactly(salvo.getId());
        assertThat(vinculoRepository.findByUsuarioIdAndEquipamentoId(
                usuario.getId(), equipamento.getId())).isPresent();
        assertThat(vinculoRepository.findByusuario_id(usuario.getId())).isPresent();
    }

    @Test
    @DisplayName("findAllByUsuarioId deve isolar vinculos de usuarios diferentes")
    void findAllByUsuarioId_deveIsolarUsuarios() {
        Usuario a = usuario("a.vinculo@exemplo.test");
        Usuario b = usuario("b.vinculo@exemplo.test");
        Equipamento equipamento = equipamento("Televisao");
        vinculo(a, equipamento, "TV A");
        vinculo(b, equipamento, "TV B");
        vinculoRepository.flush();

        assertThat(vinculoRepository.findAllByUsuarioId(a.getId()))
                .singleElement().extracting(v -> v.getUsuario().getId()).isEqualTo(a.getId());
    }
}
