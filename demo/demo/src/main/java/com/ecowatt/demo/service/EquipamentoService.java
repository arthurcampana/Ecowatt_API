package com.ecowatt.demo.service;

import com.ecowatt.demo.model.Equipamento;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class EquipamentoService {

    private final Map<Long, Equipamento> banco = new HashMap<>();
    private Long sequence = 1L;

    public Equipamento criar(Equipamento e) {
        e.setId(sequence++);
        banco.put(e.getId(), e);
        return e;
    }

    public List<Equipamento> listar() {
        return new ArrayList<>(banco.values());
    }

    public Equipamento buscar(Long id) {
        return banco.get(id);
    }
}