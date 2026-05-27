package com.ecowatt.demo.service;

import com.ecowatt.demo.dto.EquipamentoRequestDTO;
import com.ecowatt.demo.dto.EquipamentoResponseDTO;
import com.ecowatt.demo.dto.EquipamentoUpdateDTO;
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

    public EquipamentoResponseDTO criar(EquipamentoRequestDTO dto) {
        Equipamento equip = new Equipamento();

        equip.setModelo(dto.modelo());
        equip.setNome(dto.nome());
        equip.setConsumoPorHora(dto.consumoPorHora());

        equip =  equipamentoRepository.save(equip);
        return new EquipamentoResponseDTO(equip);
    }

    public List<EquipamentoResponseDTO> listar() {

        return equipamentoRepository.findAll()
                .stream()
                .map(EquipamentoResponseDTO::new)
                .toList();
    }

    public Optional<EquipamentoResponseDTO> buscar(Long id) {

        return equipamentoRepository.findById(id)
                .map(EquipamentoResponseDTO::new);
    }

    public Optional<EquipamentoResponseDTO> atualizar(
            Long id,
            EquipamentoUpdateDTO dto
    ) {

        Optional<Equipamento> opt = equipamentoRepository.findById(id);

        if (opt.isPresent()) {

            Equipamento e = opt.get();

            if (dto.nome() != null) {
                e.setNome(dto.nome());
            }

            if (dto.modelo() != null) {
                e.setModelo(dto.modelo());
            }

            if (dto.consumoPorHora() != null) {
                e.setConsumoPorHora(dto.consumoPorHora());
            }

            Equipamento atualizado = equipamentoRepository.save(e);

            return Optional.of(
                    new EquipamentoResponseDTO(atualizado)
            );
        }

        return Optional.empty();
    }
    public boolean deletar(Long id) {

        if (equipamentoRepository.existsById(id)) {

            equipamentoRepository.deleteById(id);

            return true;
        }

        return false;
    }
}