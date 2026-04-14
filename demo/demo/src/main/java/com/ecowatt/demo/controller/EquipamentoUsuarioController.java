package com.ecowatt.demo.controller;

import com.ecowatt.demo.model.Consumo;
import com.ecowatt.demo.model.Equipamento;
import com.ecowatt.demo.model.EquipamentoUsuario;
import com.ecowatt.demo.service.EquipamentoUsuarioService;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/equipamentousuario")
public class EquipamentoUsuarioController {

    private final EquipamentoUsuarioService service;

    public EquipamentoUsuarioController(EquipamentoUsuarioService service) {
        this.service = service;
    }

    @PostMapping("/add")
    public ResponseEntity<?> salvar(@RequestBody EquipamentoUsuario equipuser) {
        try {
            EquipamentoUsuario novo = service.cadastrarEquipamentoUsuario(equipuser);
            return ResponseEntity.status(HttpStatus.CREATED).body(novo);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Erro ao salvar consumo: " + e.getMessage());
        }
    }

    @GetMapping("/listar/{id}")
    public ResponseEntity<?> listar(@PathVariable Long id) {
        try {
            List<EquipamentoUsuario> lista = service.listar(id);
            return ResponseEntity.ok(lista);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao listar equipamento DO usuario");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscar(@PathVariable Long id) {
        try {
            return service.buscarPorId(id)
                    .<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body("Equipamento DO usuario não encontrado"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao buscar equipamento DO usuario ");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        try {
            boolean removido = service.deletar(id);

            if (removido)
                return ResponseEntity.ok("Removido com sucesso");

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Equipamento DO usuario não encontrado");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao deletar equipamento DO usuario");
        }
    }
}


