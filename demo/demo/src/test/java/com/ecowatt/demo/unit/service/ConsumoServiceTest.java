package com.ecowatt.demo.unit.service;

import com.ecowatt.demo.dto.ConsumoRequestDTO;
import com.ecowatt.demo.dto.ConsumoResponseDTO;
import com.ecowatt.demo.model.Consumo;
import com.ecowatt.demo.model.Usuario;
import com.ecowatt.demo.repository.ConsumoRepository;
import com.ecowatt.demo.repository.UsuarioRepository;
import com.ecowatt.demo.service.ConsumoService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsumoServiceTest {

    @Mock
    private ConsumoRepository consumoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private ConsumoService consumoService;

    private Usuario usuario() {
        return new Usuario(1L, "Usuario A", "hash", "usuario.a@exemplo.test", LocalDate.now());
    }

    @Test
    @DisplayName("salvar deve preservar a data informada e associar o usuario")
    void salvar_quandoDataInformada_devePreservarData() {
        LocalDateTime data = LocalDateTime.of(2026, 3, 10, 8, 0);
        ConsumoRequestDTO dto = new ConsumoRequestDTO(1L, 42.5, data);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario()));
        when(consumoRepository.save(any(Consumo.class))).thenAnswer(inv -> {
            Consumo c = inv.getArgument(0);
            c.setId(10L);
            return c;
        });

        ConsumoResponseDTO resposta = consumoService.salvar(dto);

        assertThat(resposta.consumoKwh()).isEqualTo(42.5);
        assertThat(resposta.dataRegistro()).isEqualTo(data);
        assertThat(resposta.usuarioId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("salvar deve preencher a data quando nao informada")
    void salvar_quandoSemData_devePreencherDataAtual() {
        ConsumoRequestDTO dto = new ConsumoRequestDTO(1L, 10.0, null);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario()));
        when(consumoRepository.save(any(Consumo.class))).thenAnswer(inv -> inv.getArgument(0));

        LocalDateTime antes = LocalDateTime.now().minusMinutes(1);
        ConsumoResponseDTO resposta = consumoService.salvar(dto);
        LocalDateTime depois = LocalDateTime.now().plusMinutes(1);

        assertThat(resposta.dataRegistro()).isBetween(antes, depois);
    }

    @Test
    @DisplayName("salvar deve falhar quando o usuario nao existe")
    void salvar_quandoUsuarioInexistente_deveLancarExcecao() {
        ConsumoRequestDTO dto = new ConsumoRequestDTO(99L, 10.0, null);
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> consumoService.salvar(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Usuário não encontrado");
        verify(consumoRepository, never()).save(any());
    }

    @Test
    @DisplayName("listarPorUsuario deve retornar somente consumos do usuario informado")
    void listarPorUsuario_quandoUsuarioExiste_deveRetornarConsumos() {
        Usuario usuario = usuario();
        Consumo consumo = new Consumo(5L, usuario, LocalDateTime.now(), 30.0);
        when(usuarioRepository.existsById(1L)).thenReturn(true);
        when(consumoRepository.findByUsuarioId(1L)).thenReturn(List.of(consumo));

        List<ConsumoResponseDTO> resposta = consumoService.listarPorUsuario(1L);

        assertThat(resposta).hasSize(1);
        assertThat(resposta.get(0).usuarioId()).isEqualTo(1L);
        assertThat(resposta.get(0).consumoKwh()).isEqualTo(30.0);
    }

    @Test
    @DisplayName("listarPorUsuario deve falhar quando o usuario nao existe")
    void listarPorUsuario_quandoUsuarioInexistente_deveLancarExcecao() {
        when(usuarioRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> consumoService.listarPorUsuario(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Usuário não encontrado");
        verify(consumoRepository, never()).findByUsuarioId(any());
    }

    @Test
    @DisplayName("deletar deve retornar false quando o id nao existe")
    void deletar_quandoIdInexistente_deveRetornarFalse() {
        when(consumoRepository.existsById(77L)).thenReturn(false);

        boolean resultado = consumoService.deletar(77L);

        assertThat(resultado).isFalse();
        verify(consumoRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("salvar deve gravar o valor de consumo no objeto persistido")
    void salvar_deveGravarConsumoNoObjetoSalvo() {
        ConsumoRequestDTO dto = new ConsumoRequestDTO(1L, 55.0, null);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario()));
        when(consumoRepository.save(any(Consumo.class))).thenAnswer(inv -> inv.getArgument(0));

        consumoService.salvar(dto);

        ArgumentCaptor<Consumo> captor = ArgumentCaptor.forClass(Consumo.class);
        verify(consumoRepository).save(captor.capture());
        assertThat(captor.getValue().getConsumoKwh()).isEqualTo(55.0);
        assertThat(captor.getValue().getUsuario().getId()).isEqualTo(1L);
    }
}
