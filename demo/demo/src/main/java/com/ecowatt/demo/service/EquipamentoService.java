package com.ecowatt.demo.service;

import com.ecowatt.demo.model.Equipamento;
import com.ecowatt.demo.repository.EquipamentoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EquipamentoService {

    private final EquipamentoRepository equipamentoRepository;

    public EquipamentoService(EquipamentoRepository equipamentoRepository){
        this.equipamentoRepository = equipamentoRepository;
    }

    public Equipamento criar(Equipamento e) {
        return equipamentoRepository.save(e);
    }

    public List<Equipamento> listar() {
        return equipamentoRepository.findAll();
    }

    public Optional<Equipamento> buscar(Long id) {
        return equipamentoRepository.findById(id);
    }

    public Optional<Equipamento> atualizar(Long id, Equipamento atualizado){
        Optional<Equipamento> opt = equipamentoRepository.findById(id);

        if(opt.isPresent()){
            Equipamento e = opt.get();
            e.setNome(atualizado.getNome());
            e.setModelo(atualizado.getModelo());
            e.setConsumoPorHora(atualizado.getConsumoPorHora());

            return Optional.of(equipamentoRepository.save(e));
        }

        return Optional.empty();
    }

    public boolean deletar(Long id){
        if(equipamentoRepository.existsById(id)){
            equipamentoRepository.deleteById(id);
            return true;
        }
        return false;
    }
}