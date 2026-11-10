package com.ecowatt.demo.controller;

import com.ecowatt.demo.service.PrevisaoService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/previsao")
public class PrevisaoController {

    private final PrevisaoService service;

    public PrevisaoController(PrevisaoService service) {
        this.service = service;
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
}
