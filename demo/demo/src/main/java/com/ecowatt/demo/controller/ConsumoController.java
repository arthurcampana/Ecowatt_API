package com.ecowatt.demo.controller;

import com.ecowatt.demo.dto.ConsumoRequestDTO;
import com.ecowatt.demo.dto.ConsumoResponseDTO;
import com.ecowatt.demo.dto.ConsumoUpdateDTO;
import com.ecowatt.demo.service.ConsumoService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/consumo")
public class ConsumoController {

    private final ConsumoService service;

    public ConsumoController(ConsumoService service) {
        this.service = service;
    }

    // CREATE
    @PostMapping("/add")
    public ResponseEntity<?> salvar(
           @Valid @RequestBody ConsumoRequestDTO dto
    ) {

        try {

            ConsumoResponseDTO novo = service.salvar(dto);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(novo);

        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Erro ao salvar consumo: " + e.getMessage());
        }
    }

    // READ ALL
    @GetMapping("/listar")
    public ResponseEntity<?> listar() {

        try {

            return ResponseEntity.ok(service.listar());

        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao listar consumos");
        }
    }

    // READ ONE
    @GetMapping("/{id}")
    public ResponseEntity<?> buscar(@PathVariable Long id) {

        try {

            return service.buscarPorId(id)
                    .<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body("Consumo não encontrado"));

        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao buscar consumo");
        }
    }

    // READ BY USER
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<?> listarPorUsuario(
            @PathVariable Long usuarioId
    ) {

        try {

            return ResponseEntity.ok(
                    service.listarPorUsuario(usuarioId)
            );

        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {

        try {

            boolean removido = service.deletar(id);

            if (removido) {
                return ResponseEntity.ok("Removido com sucesso");
            }

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Consumo não encontrado");

        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao deletar consumo");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> alterar(
            @PathVariable Long id,
            @Valid @RequestBody ConsumoUpdateDTO dto
    ) {

        try {

            return service.alterar(id, dto)
                    .<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body("Consumo não encontrado"));

        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Erro ao alterar consumo");
        }
    }
}