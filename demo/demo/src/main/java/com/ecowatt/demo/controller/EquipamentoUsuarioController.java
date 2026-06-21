package com.ecowatt.demo.controller;

import com.ecowatt.demo.dto.EquipamentoUsuarioRequestDTO;
import com.ecowatt.demo.dto.EquipamentoUsuarioResponseDTO;
import com.ecowatt.demo.dto.EquipamentoUsuarioUpdateDTO;
import com.ecowatt.demo.service.EquipamentoUsuarioService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/equipamentousuario")
public class EquipamentoUsuarioController {

    private final EquipamentoUsuarioService service;

    public EquipamentoUsuarioController(
            EquipamentoUsuarioService service
    ) {
        this.service = service;
    }

    // CREATE
    @PostMapping("/add")
    public ResponseEntity<?> salvar(
            @Valid @RequestBody
            EquipamentoUsuarioRequestDTO dto
    ) {
        try {

            EquipamentoUsuarioResponseDTO novo =
                    service.cadastrarEquipamentoUsuario(dto);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(novo);

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Erro ao salvar equipamento: "
                            + e.getMessage());
        }
    }

    // LISTAR POR USUÁRIO
    @GetMapping("/listar/{id}")
    public ResponseEntity<?> listar(
            @PathVariable Long id
    ) {

        try {

            return ResponseEntity.ok(
                    service.listar(id)
            );

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao listar equipamentos");
        }
    }

    // BUSCAR POR ID
    @GetMapping("/{id}")
    public ResponseEntity<?> buscar(
            @PathVariable Long id
    ) {

        try {

            return service.buscarPorId(id)
                    .<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElseGet(() ->
                            ResponseEntity
                                    .status(HttpStatus.NOT_FOUND)
                                    .body("Equipamento não encontrado"));

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao buscar equipamento");
        }
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(

            @PathVariable Long id,

            @Valid @RequestBody
            EquipamentoUsuarioUpdateDTO dto

    ) {

        try {

            return service.atualizar(id, dto)
                    .<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElseGet(() ->

                            ResponseEntity
                                    .status(HttpStatus.NOT_FOUND)
                                    .body("Equipamento não encontrado")
                    );

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Erro ao atualizar equipamento: "
                            + e.getMessage());
        }
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(
            @PathVariable Long id
    ) {

        try {

            boolean removido =
                    service.deletar(id);

            if (removido) {

                return ResponseEntity.ok(
                        "Removido com sucesso"
                );
            }

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Equipamento não encontrado");

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao deletar equipamento");
        }
    }
}