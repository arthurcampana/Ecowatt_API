package com.ecowatt.demo.service;

import com.ecowatt.demo.dto.ConsumoRequestDTO;
import com.ecowatt.demo.dto.ConsumoResponseDTO;
import com.ecowatt.demo.dto.ConsumoUpdateDTO;
import com.ecowatt.demo.model.Consumo;
import com.ecowatt.demo.model.Usuario;
import com.ecowatt.demo.repository.ConsumoRepository;
import com.ecowatt.demo.repository.UsuarioRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ConsumoService {

    private final ConsumoRepository consumoRepository;
    private final UsuarioRepository usuarioRepository;

    public ConsumoService(
            ConsumoRepository consumoRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.consumoRepository = consumoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // CREATE
    public ConsumoResponseDTO salvar(ConsumoRequestDTO dto) {

        Usuario usuario = usuarioRepository.findById(dto.usuarioId())
                .orElseThrow(() ->
                        new RuntimeException("Usuário não encontrado"));

        Consumo consumo = new Consumo();

        consumo.setUsuario(usuario);
        consumo.setConsumoKwh(dto.consumoKwh());

        if (dto.dataRegistro() != null) {
            consumo.setDataRegistro(dto.dataRegistro());
        } else {
            consumo.setDataRegistro(LocalDateTime.now());
        }

        consumo = consumoRepository.save(consumo);

        return new ConsumoResponseDTO(consumo);
    }

    // READ ALL
    public List<ConsumoResponseDTO> listar() {

        return consumoRepository.findAll()
                .stream()
                .map(ConsumoResponseDTO::new)
                .toList();
    }

    // READ ONE
    public Optional<ConsumoResponseDTO> buscarPorId(Long id) {

        if (id == null) {
            throw new RuntimeException("ID inválido");
        }

        return consumoRepository.findById(id)
                .map(ConsumoResponseDTO::new);
    }

    // READ BY USER
    public List<ConsumoResponseDTO> listarPorUsuario(Long usuarioId) {

        if (usuarioId == null) {
            throw new RuntimeException("ID do usuário inválido");
        }

        if (!usuarioRepository.existsById(usuarioId)) {
            throw new RuntimeException("Usuário não encontrado");
        }

        return consumoRepository.findByUsuarioId(usuarioId)
                .stream()
                .map(ConsumoResponseDTO::new)
                .toList();
    }

    // DELETE
    public boolean deletar(Long id) {

        if (!consumoRepository.existsById(id)) {
            return false;
        }

        consumoRepository.deleteById(id);

        return true;
    }

    // UPDATE
    public Optional<ConsumoResponseDTO> alterar(
            Long id,
            ConsumoUpdateDTO dto
    ) {

        return consumoRepository.findById(id)
                .map(consumo -> {

                    if (dto.consumoKwh() != null) {
                        consumo.setConsumoKwh(dto.consumoKwh());
                    }

                    if (dto.dataRegistro() != null) {
                        consumo.setDataRegistro(dto.dataRegistro());
                    }

                    consumoRepository.save(consumo);

                    return new ConsumoResponseDTO(consumo);
                });
    }
}