package com.ecowatt.demo.service;

import com.ecowatt.demo.dto.EquipamentoUsuarioRequestDTO;
import com.ecowatt.demo.dto.EquipamentoUsuarioResponseDTO;
import com.ecowatt.demo.dto.EquipamentoUsuarioUpdateDTO;
import com.ecowatt.demo.model.Equipamento;
import com.ecowatt.demo.model.EquipamentoUsuario;
import com.ecowatt.demo.model.Usuario;
import com.ecowatt.demo.repository.EquipamentoRepository;
import com.ecowatt.demo.repository.EquipamentoUsuarioRepository;
import com.ecowatt.demo.repository.UsuarioRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EquipamentoUsuarioService {

    private final EquipamentoUsuarioRepository equipamentoUsuarioRepository;
    private final EquipamentoRepository equipRepository;
    private final UsuarioRepository userRepository;

    public EquipamentoUsuarioService(
            EquipamentoUsuarioRepository equipamentoUsuarioRepository,
            EquipamentoRepository equipRepository,
            UsuarioRepository userRepository
    ) {

        this.equipamentoUsuarioRepository =
                equipamentoUsuarioRepository;

        this.equipRepository =
                equipRepository;

        this.userRepository =
                userRepository;
    }

    // CREATE
    public EquipamentoUsuarioResponseDTO cadastrarEquipamentoUsuario(
            EquipamentoUsuarioRequestDTO dto
    ) {

        if (dto == null) {
            throw new RuntimeException("Dados inválidos");
        }

        Usuario usuario =
                userRepository.findById(dto.usuarioId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Usuário não encontrado"
                                ));

        Equipamento equipamento =
                equipRepository.findById(dto.equipamentoId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Equipamento não encontrado"
                                ));

        EquipamentoUsuario equipamentoUsuario =
                new EquipamentoUsuario();

        equipamentoUsuario.setUsuario(usuario);

        equipamentoUsuario.setEquipamento(equipamento);

        equipamentoUsuario.setNomeIdentificacao(
                dto.nomeIdentificacao()
        );

        equipamentoUsuario.setHorasPorDia(
                dto.horasPorDia()
        );

        double esperado =
                dto.horasPorDia()
                        * equipamento.getConsumoPorHora();

        equipamentoUsuario.setConsumoEsperado(
                esperado
        );

        equipamentoUsuario =
                equipamentoUsuarioRepository
                        .save(equipamentoUsuario);

        return new EquipamentoUsuarioResponseDTO(
                equipamentoUsuario
        );
    }

    // UPDATE
    public Optional<EquipamentoUsuarioResponseDTO> atualizar(
            Long id,
            EquipamentoUsuarioUpdateDTO dto
    ) {

        return equipamentoUsuarioRepository
                .findById(id)
                .map(equipamentoUsuario -> {

                    if (dto.nomeIdentificacao() != null) {

                        equipamentoUsuario.setNomeIdentificacao(
                                dto.nomeIdentificacao()
                        );
                    }

                    if (dto.horasPorDia() != null) {

                        equipamentoUsuario.setHorasPorDia(
                                dto.horasPorDia()
                        );
                    }

                    if (dto.equipamentoId() != null) {

                        Equipamento equipamento =
                                equipRepository
                                        .findById(dto.equipamentoId())
                                        .orElseThrow(() ->
                                                new RuntimeException(
                                                        "Equipamento não encontrado"
                                                ));

                        equipamentoUsuario.setEquipamento(
                                equipamento
                        );

                        double esperado =
                                equipamentoUsuario.getHorasPorDia()
                                        * equipamento.getConsumoPorHora();

                        equipamentoUsuario.setConsumoEsperado(
                                esperado
                        );
                    }

                    equipamentoUsuario =
                            equipamentoUsuarioRepository.save(
                                    equipamentoUsuario
                            );

                    return new EquipamentoUsuarioResponseDTO(
                            equipamentoUsuario
                    );
                });
    }

    // LISTAR POR USUÁRIO
    public List<EquipamentoUsuarioResponseDTO> listar(
            Long usuarioId
    ) {

        return equipamentoUsuarioRepository
                .findAllByUsuarioId(usuarioId)
                .stream()
                .map(EquipamentoUsuarioResponseDTO::new)
                .toList();
    }

    // BUSCAR POR ID
    public Optional<EquipamentoUsuarioResponseDTO> buscarPorId(
            Long id
    ) {

        if (id == null) {
            throw new RuntimeException("ID inválido");
        }

        return equipamentoUsuarioRepository
                .findById(id)
                .map(EquipamentoUsuarioResponseDTO::new);
    }

    // DELETE
    public boolean deletar(Long id) {

        if (!equipamentoUsuarioRepository.existsById(id)) {
            return false;
        }

        equipamentoUsuarioRepository.deleteById(id);

        return true;
    }
}