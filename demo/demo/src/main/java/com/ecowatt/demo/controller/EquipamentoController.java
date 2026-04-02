package com.ecowatt.demo.controller;

import com.ecowatt.demo.model.Equipamento;
import com.ecowatt.demo.service.EquipamentoService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/equipamentos")
public class EquipamentoController {

    private final EquipamentoService service;

    public EquipamentoController(EquipamentoService service) {
        this.service = service;
    }

    @PostMapping("/criar")
    public ResponseEntity<?> criar(@RequestBody Equipamento e) {
        try {
            if (e.getNome() == null || e.getConsumoPorHora() == null) {
                return ResponseEntity.badRequest().body("Nome e consumo são obrigatórios");
            }

            return ResponseEntity.status(201).body(service.criar(e));

        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body("Erro ao criar equipamento");
        }
    }

    @GetMapping("/listar")
    public ResponseEntity<?> listar() {
        try {
            return ResponseEntity.ok(service.listar());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro ao listar equipamentos");
        }
    }

    @GetMapping("/buscar/{id}")
    public ResponseEntity<?> buscar(@PathVariable Long id) {
        try {
            Equipamento eq = service.buscar(id);

            if (eq == null) {
                return ResponseEntity.status(404).body("Equipamento não encontrado");
            }

            return ResponseEntity.ok(eq);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro ao buscar equipamento");
        }
    }
}