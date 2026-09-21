package com.ecowatt.demo.unit.service;

import com.ecowatt.demo.dto.ConfiguracaoRequestDTO;
import com.ecowatt.demo.dto.ConfiguracaoResponseDTO;
import com.ecowatt.demo.dto.ConfiguracaoUpdateDTO;
import com.ecowatt.demo.model.Configuracao;
import com.ecowatt.demo.model.Usuario;
import com.ecowatt.demo.repository.ConfiguracaoRepository;
import com.ecowatt.demo.repository.UsuarioRepository;
import com.ecowatt.demo.service.ConfiguracaoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfiguracaoServiceTest {

    @Mock
    private ConfiguracaoRepository configuracaoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private ConfiguracaoService service;

    private Usuario usuario() {
        return new Usuario(1L, "Usuario A", "hash", "usuario.a@exemplo.test", LocalDate.now());
    }

    private Configuracao configuracao() {
        return new Configuracao(5L, usuario(), new BigDecimal("0.85"), 300.0, null, "kWh");
    }

    @Test
    @DisplayName("salvar deve associar usuario, tarifa, meta e unidade")
    void salvar_quandoDadosValidos_devePersistirConfiguracao() {
        ConfiguracaoRequestDTO dto = new ConfiguracaoRequestDTO(
                1L, new BigDecimal("0.85"), 300.0, "kWh");
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario()));
        when(configuracaoRepository.save(any(Configuracao.class))).thenAnswer(invocation -> {
            Configuracao config = invocation.getArgument(0);
            config.setId(5L);
            return config;
        });

        ConfiguracaoResponseDTO resposta = service.salvar(dto);

        ArgumentCaptor<Configuracao> captor = ArgumentCaptor.forClass(Configuracao.class);
        verify(configuracaoRepository).save(captor.capture());
        assertThat(captor.getValue().getCliente().getId()).isEqualTo(1L);
        assertThat(captor.getValue().getValorTarifa()).isEqualByComparingTo("0.85");
        assertThat(captor.getValue().getMeta()).isEqualTo(300.0);
        assertThat(resposta.id()).isEqualTo(5L);
    }

    @Test
    @DisplayName("salvar deve falhar quando o usuario nao existe")
    void salvar_quandoUsuarioInexistente_deveLancarExcecao() {
        ConfiguracaoRequestDTO dto = new ConfiguracaoRequestDTO(
                99L, BigDecimal.ONE, 100.0, "kWh");
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.salvar(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Usuário não encontrado");
        verify(configuracaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("buscar deve rejeitar id nulo")
    void buscar_quandoIdNulo_deveLancarExcecao() {
        assertThatThrownBy(() -> service.buscarConfig(null))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("ID inválido");
    }

    @Test
    @DisplayName("alterar deve modificar somente os campos informados")
    void alterar_quandoParcial_devePreservarCamposOmitidos() {
        Configuracao existente = configuracao();
        ConfiguracaoUpdateDTO dto = new ConfiguracaoUpdateDTO(
                new BigDecimal("1.10"), null, null);
        when(configuracaoRepository.findById(5L)).thenReturn(Optional.of(existente));
        when(configuracaoRepository.save(existente)).thenReturn(existente);

        ConfiguracaoResponseDTO resposta = service.alterarConfig(5L, dto).orElseThrow();

        assertThat(resposta.valorTarifa()).isEqualByComparingTo("1.10");
        assertThat(resposta.meta()).isEqualTo(300.0);
        assertThat(resposta.unidadeMedida()).isEqualTo("kWh");
    }

    @Test
    @DisplayName("alterar nao deve salvar quando o id nao existe")
    void alterar_quandoIdInexistente_deveRetornarVazio() {
        when(configuracaoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(service.alterarConfig(99L,
                new ConfiguracaoUpdateDTO(BigDecimal.ONE, 100.0, "kWh"))).isEmpty();
        verify(configuracaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("buscar por usuario deve delegar a consulta e converter resposta")
    void buscarPorUsuario_quandoExiste_deveRetornarConfiguracao() {
        when(configuracaoRepository.findByUsuarioId(1L)).thenReturn(Optional.of(configuracao()));

        ConfiguracaoResponseDTO resposta = service.buscarPorUsuario(1L).orElseThrow();

        assertThat(resposta.usuarioid()).isEqualTo(1L);
        verify(configuracaoRepository).findByUsuarioId(1L);
    }

    @Test
    @DisplayName("excluir deve retornar falso sem tentar remover id inexistente")
    void excluir_quandoInexistente_deveRetornarFalso() {
        when(configuracaoRepository.existsById(99L)).thenReturn(false);

        assertThat(service.excluirConfig(99L)).isFalse();
        verify(configuracaoRepository, never()).deleteById(any());
    }
}
