package com.ecowatt.demo.service;

import com.ecowatt.demo.model.EquipamentoUsuario;
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

    public EquipamentoUsuarioService(EquipamentoUsuarioRepository equipamentoUsuarioRepository, EquipamentoRepository equipRepository, UsuarioRepository userRepository) {
        this.equipamentoUsuarioRepository = equipamentoUsuarioRepository;
        this.equipRepository = equipRepository;
        this.userRepository = userRepository;
    }

    public EquipamentoUsuario cadastrarEquipamentoUsuario(EquipamentoUsuario equipuser) {

        if (equipuser == null)
            throw new RuntimeException("não pode ser nulo");

        if (equipuser.getUsuario() == null || equipuser.getUsuario().getId() == null)
            throw new RuntimeException("Usuário obrigatório");

        //🔥
        if (equipuser.getEquipamento() == null || equipuser.getEquipamento().getId() == null)
            equipRepository.save(equipuser.getEquipamento());

        Long usuarioId = equipuser.getUsuario().getId();
        Long equipamentoId = equipuser.getEquipamento().getId();

        var usuario = userRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        var equip = equipRepository.findById(equipamentoId)
                .orElseThrow(() -> new RuntimeException("Equipamento não encontrado"));

        equipuser.setUsuario(usuario);
        equipuser.setEquipamento(equip);

        return equipamentoUsuarioRepository.save(equipuser);
    }


    public Optional<EquipamentoUsuario> atualizar(Long id, EquipamentoUsuario atualizado){
        Optional<EquipamentoUsuario> opt = equipamentoUsuarioRepository.findById(id);

        if(opt.isPresent()){
            EquipamentoUsuario e = opt.get();
            e.setNomeIdentificacao(atualizado.getNomeIdentificacao());
            e.setConsumoEsperado(atualizado.getConsumoEsperado());
            e.setHorasPorDia(atualizado.getHorasPorDia());
            return Optional.of(equipamentoUsuarioRepository.save(e));
        }

        return Optional.empty();
    }

    public List<EquipamentoUsuario> listar(Long id) {
        return equipamentoUsuarioRepository.findAllByUsuarioId(id);
    }

    public Optional<EquipamentoUsuario> buscarPorId(Long id) {
        if (id == null)
            throw new RuntimeException("ID inválido");

        return equipamentoUsuarioRepository.findById(id);
    }

    public boolean deletar(Long id) {
        if (!equipamentoUsuarioRepository.existsById(id))
            return false;

        equipamentoUsuarioRepository.deleteById(id);
        return true;
    }
}

