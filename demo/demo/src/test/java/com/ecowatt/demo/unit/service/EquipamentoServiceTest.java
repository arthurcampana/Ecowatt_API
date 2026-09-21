package com.ecowatt.demo.unit.service;

import com.ecowatt.demo.dto.EquipamentoRequestDTO;
import com.ecowatt.demo.dto.EquipamentoResponseDTO;
import com.ecowatt.demo.dto.EquipamentoUpdateDTO;
import com.ecowatt.demo.model.Equipamento;
import com.ecowatt.demo.repository.EquipamentoRepository;
import com.ecowatt.demo.service.EquipamentoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EquipamentoServiceTest {

    @Mock
    private EquipamentoRepository repository;

    @InjectMocks
    private EquipamentoService service;

    @Test
    @DisplayName("criar deve copiar os dados e retornar o equipamento salvo")
    void criar_quandoDadosValidos_deveSalvarEquipamento() {
        EquipamentoRequestDTO dto = new EquipamentoRequestDTO("Chuveiro", "X1", 5.5);
        when(repository.save(any(Equipamento.class))).thenAnswer(invocation -> {
            Equipamento equipamento = invocation.getArgument(0);
            equipamento.setId(1L);
            return equipamento;
        });

        EquipamentoResponseDTO resposta = service.criar(dto);

        ArgumentCaptor<Equipamento> captor = ArgumentCaptor.forClass(Equipamento.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getNome()).isEqualTo("Chuveiro");
        assertThat(captor.getValue().getModelo()).isEqualTo("X1");
        assertThat(captor.getValue().getConsumoPorHora()).isEqualTo(5.5);
        assertThat(resposta.id()).isEqualTo(1L);
    }

    @Test
    @DisplayName("listar deve converter todos os equipamentos mantendo a ordem")
    void listar_quandoExistemEquipamentos_deveConverterTodos() {
        when(repository.findAll()).thenReturn(List.of(
                new Equipamento(1L, "A", "M1", 1.0),
                new Equipamento(2L, "B", "M2", 2.0)));

        List<EquipamentoResponseDTO> resposta = service.listar();

        assertThat(resposta).extracting(EquipamentoResponseDTO::nome)
                .containsExactly("A", "B");
    }

    @Test
    @DisplayName("buscar deve retornar vazio quando o equipamento nao existe")
    void buscar_quandoInexistente_deveRetornarVazio() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThat(service.buscar(99L)).isEmpty();
    }

    @Test
    @DisplayName("atualizar deve alterar somente os campos informados")
    void atualizar_quandoParcial_devePreservarCamposOmitidos() {
        Equipamento existente = new Equipamento(1L, "Antigo", "Modelo", 1.0);
        EquipamentoUpdateDTO dto = new EquipamentoUpdateDTO("Novo", null, 2.5);
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.save(existente)).thenReturn(existente);

        EquipamentoResponseDTO resposta = service.atualizar(1L, dto).orElseThrow();

        assertThat(resposta.nome()).isEqualTo("Novo");
        assertThat(resposta.modelo()).isEqualTo("Modelo");
        assertThat(resposta.consumoPorHora()).isEqualTo(2.5);
    }

    @Test
    @DisplayName("atualizar nao deve salvar quando o id nao existe")
    void atualizar_quandoInexistente_deveRetornarVazio() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThat(service.atualizar(99L, new EquipamentoUpdateDTO("X", null, null))).isEmpty();
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("deletar deve remover somente quando o id existe")
    void deletar_quandoExiste_deveRemover() {
        when(repository.existsById(1L)).thenReturn(true);

        assertThat(service.deletar(1L)).isTrue();
        verify(repository).deleteById(1L);
    }

    @Test
    @DisplayName("deletar deve retornar falso quando o id nao existe")
    void deletar_quandoInexistente_deveRetornarFalso() {
        when(repository.existsById(99L)).thenReturn(false);

        assertThat(service.deletar(99L)).isFalse();
        verify(repository, never()).deleteById(any());
    }
}
