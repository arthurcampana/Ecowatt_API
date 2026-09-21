package com.ecowatt.demo.unit.service;

import com.ecowatt.demo.dto.EquipamentoUsuarioRequestDTO;
import com.ecowatt.demo.dto.EquipamentoUsuarioResponseDTO;
import com.ecowatt.demo.dto.EquipamentoUsuarioUpdateDTO;
import com.ecowatt.demo.model.Equipamento;
import com.ecowatt.demo.model.EquipamentoUsuario;
import com.ecowatt.demo.model.Usuario;
import com.ecowatt.demo.repository.EquipamentoRepository;
import com.ecowatt.demo.repository.EquipamentoUsuarioRepository;
import com.ecowatt.demo.repository.UsuarioRepository;
import com.ecowatt.demo.service.EquipamentoUsuarioService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EquipamentoUsuarioServiceTest {

    @Mock
    private EquipamentoUsuarioRepository equipamentoUsuarioRepository;

    @Mock
    private EquipamentoRepository equipamentoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private EquipamentoUsuarioService service;

    private Usuario usuario() {
        return new Usuario(1L, "Usuario A", "hash", "usuario.a@exemplo.test", LocalDate.now());
    }

    private Equipamento equipamento(Long id, double consumoPorHora) {
        return new Equipamento(id, "Equipamento " + id, "Modelo", consumoPorHora);
    }

    private EquipamentoUsuario vinculo(Equipamento equipamento, double horasPorDia) {
        EquipamentoUsuario vinculo = new EquipamentoUsuario();
        vinculo.setId(10L);
        vinculo.setUsuario(usuario());
        vinculo.setEquipamento(equipamento);
        vinculo.setNomeIdentificacao("Equipamento principal");
        vinculo.setHorasPorDia(horasPorDia);
        vinculo.setConsumoEsperado(horasPorDia * equipamento.getConsumoPorHora());
        return vinculo;
    }

    @Test
    @DisplayName("cadastrar deve calcular consumo esperado usando horas por dia e consumo por hora")
    void cadastrar_quandoDadosValidos_deveCalcularConsumoEsperado() {
        Equipamento equipamento = equipamento(2L, 1.5);
        EquipamentoUsuarioRequestDTO dto = new EquipamentoUsuarioRequestDTO(
                "Computador", 4.0, 999.0, 1L, 2L);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario()));
        when(equipamentoRepository.findById(2L)).thenReturn(Optional.of(equipamento));
        when(equipamentoUsuarioRepository.save(any(EquipamentoUsuario.class)))
                .thenAnswer(invocation -> {
                    EquipamentoUsuario salvo = invocation.getArgument(0);
                    salvo.setId(10L);
                    return salvo;
                });

        EquipamentoUsuarioResponseDTO resposta = service.cadastrarEquipamentoUsuario(dto);

        assertThat(resposta.consumoEsperado()).isEqualTo(6.0);
        ArgumentCaptor<EquipamentoUsuario> captor = ArgumentCaptor.forClass(EquipamentoUsuario.class);
        verify(equipamentoUsuarioRepository).save(captor.capture());
        assertThat(captor.getValue().getConsumoEsperado()).isEqualTo(6.0);
    }

    @Test
    @DisplayName("cadastrar deve rejeitar DTO nulo")
    void cadastrar_quandoDtoNulo_deveLancarExcecao() {
        assertThatThrownBy(() -> service.cadastrarEquipamentoUsuario(null))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Dados inválidos");
        verify(equipamentoUsuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("cadastrar deve falhar quando o usuario nao existe")
    void cadastrar_quandoUsuarioInexistente_deveLancarExcecao() {
        EquipamentoUsuarioRequestDTO dto = new EquipamentoUsuarioRequestDTO(
                "Computador", 4.0, null, 99L, 2L);
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.cadastrarEquipamentoUsuario(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Usuário não encontrado");
        verify(equipamentoRepository, never()).findById(any());
        verify(equipamentoUsuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("atualizar equipamento deve recalcular o consumo esperado")
    void atualizar_quandoTrocaEquipamento_deveRecalcularConsumo() {
        Equipamento antigo = equipamento(2L, 1.0);
        Equipamento novo = equipamento(3L, 2.5);
        EquipamentoUsuario existente = vinculo(antigo, 4.0);
        EquipamentoUsuarioUpdateDTO dto = new EquipamentoUsuarioUpdateDTO(null, null, null, 3L);
        when(equipamentoUsuarioRepository.findById(10L)).thenReturn(Optional.of(existente));
        when(equipamentoRepository.findById(3L)).thenReturn(Optional.of(novo));
        when(equipamentoUsuarioRepository.save(any(EquipamentoUsuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        EquipamentoUsuarioResponseDTO resposta = service.atualizar(10L, dto).orElseThrow();

        assertThat(resposta.equipamentoId()).isEqualTo(3L);
        assertThat(resposta.consumoEsperado()).isEqualTo(10.0);
    }

    @Test
    @DisplayName("REGRESSAO: atualizar horas deve recalcular o consumo esperado")
    void atualizar_quandoAlteraHoras_deveRecalcularConsumo() {
        Equipamento equipamento = equipamento(2L, 1.5);
        EquipamentoUsuario existente = vinculo(equipamento, 2.0);
        EquipamentoUsuarioUpdateDTO dto = new EquipamentoUsuarioUpdateDTO(null, 4.0, null, null);
        when(equipamentoUsuarioRepository.findById(10L)).thenReturn(Optional.of(existente));
        when(equipamentoUsuarioRepository.save(any(EquipamentoUsuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        EquipamentoUsuarioResponseDTO resposta = service.atualizar(10L, dto).orElseThrow();

        assertThat(resposta.horasPorDia()).isEqualTo(4.0);
        assertThat(resposta.consumoEsperado())
                .as("O consumo deve acompanhar a alteracao das horas de uso")
                .isEqualTo(6.0);
    }
}
