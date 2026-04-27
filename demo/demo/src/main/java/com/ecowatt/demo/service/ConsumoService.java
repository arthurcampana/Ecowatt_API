package com.ecowatt.demo.service;

import com.ecowatt.demo.model.Consumo;
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

    public ConsumoService(ConsumoRepository consumoRepository, UsuarioRepository usuarioRepository) {
        this.consumoRepository = consumoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public Consumo salvar(Consumo consumo) {

        if (consumo == null)
            throw new RuntimeException("Consumo não pode ser nulo");

        if (consumo.getUsuario() == null || consumo.getUsuario().getId() == null)
            throw new RuntimeException("Usuário obrigatório");

        if (consumo.getConsumoKwh() == null || consumo.getConsumoKwh() < 0)
            throw new RuntimeException("Consumo inválido");

        if (consumo.getDataRegistro() == null)
            consumo.setDataRegistro(LocalDateTime.now());

        Long usuarioId = consumo.getUsuario().getId();

        var usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        consumo.setUsuario(usuario);

        return consumoRepository.save(consumo);
    }

    public List<Consumo> listar() {
        return consumoRepository.findAll();
    }

    public Optional<Consumo> buscarPorId(Long id) {
        if (id == null)
            throw new RuntimeException("ID inválido");

        return consumoRepository.findById(id);
    }

    public List<Consumo> listarPorUsuario(Long usuarioId) {

        if (usuarioId == null)
            throw new RuntimeException("ID do usuário inválido");

        // opcional, mas correto: validar existência do usuário
        if (!usuarioRepository.existsById(usuarioId))
            throw new RuntimeException("Usuário não encontrado");

        return consumoRepository.findByUsuarioId(usuarioId);
    }

    public boolean deletar(Long id) {
        if (!consumoRepository.existsById(id))
            return false;

        consumoRepository.deleteById(id);
        return true;
    }
}