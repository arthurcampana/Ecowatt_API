package com.ecowatt.demo.controller;

import com.ecowatt.demo.dto.EquipamentoRequestDTO;
import com.ecowatt.demo.dto.EquipamentoResponseDTO;
import com.ecowatt.demo.dto.EquipamentoUpdateDTO;
import com.ecowatt.demo.model.Equipamento;
import com.ecowatt.demo.service.EquipamentoService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/equipamentos")
public class EquipamentoController {

    private final EquipamentoService service;

    public EquipamentoController(EquipamentoService service) {
        this.service = service;
    }

    @PostMapping("/add")
    public ResponseEntity<?> criar(@Valid @RequestBody EquipamentoRequestDTO e) {
        try {
            if (e.nome() == null || e.consumoPorHora() == null) {
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
            Optional<EquipamentoResponseDTO> eqOpt = service.buscar(id);

            if (eqOpt.isPresent()) {
                return ResponseEntity.ok(eqOpt.get());
            }

            return ResponseEntity.status(404).body("Equipamento não encontrado");

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro ao buscar equipamento");
        }
    }
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody EquipamentoUpdateDTO dto
    ) {

        try {

            Optional<EquipamentoResponseDTO> equipamento =
                    service.atualizar(id, dto);

            return equipamento.<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(404)
                            .body("Equipamento não encontrado"));

        } catch (Exception e) {

            return ResponseEntity.internalServerError()
                    .body("Erro ao atualizar equipamento");
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {

        try {

            boolean removido = service.deletar(id);

            if (removido) {
                return ResponseEntity.ok("Equipamento removido com sucesso");
            }

            return ResponseEntity.status(404)
                    .body("Equipamento não encontrado");

        } catch (Exception e) {

            return ResponseEntity.internalServerError()
                    .body("Erro ao remover equipamento");
        }
    }
}