package com.ecowatt.demo.service;

import com.ecowatt.demo.dto.PrevisaoConsumoDTO;
import com.ecowatt.demo.repository.PrevisaoConsumoRepository;
import com.ecowatt.demo.repository.UsuarioRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PrevisaoService {

    private final PrevisaoConsumoRepository previsaoConsumoRepository;
    private final UsuarioRepository usuarioRepository;

    public PrevisaoService(
            PrevisaoConsumoRepository previsaoConsumoRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.previsaoConsumoRepository = previsaoConsumoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // READ BY USER
    public List<PrevisaoConsumoDTO> listarPorUsuario(Long usuarioId) {

        if (usuarioId == null) {
            throw new RuntimeException("ID do usuário inválido");
        }

        if (!usuarioRepository.existsById(usuarioId)) {
            throw new RuntimeException("Usuário não encontrado");
        }

        return previsaoConsumoRepository.findByUsuarioIdOrderByAnoMes(usuarioId)
                .stream()
                .map(PrevisaoConsumoDTO::new)
                .toList();
    }
}
