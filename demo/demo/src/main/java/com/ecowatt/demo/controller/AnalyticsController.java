package com.ecowatt.demo.controller;

import com.ecowatt.demo.dto.EventoCliqueDTO;
import com.ecowatt.demo.service.AnalyticsService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Expõe os dados de analytics de uso da API:
 *  - registro de cliques de interface (POST);
 *  - ranking de endpoints mais usados;
 *  - ranking de cliques (onde os usuários mais clicam);
 *  - mapa de calor de acessos (dia da semana x hora).
 *
 * Todas as rotas são autenticadas por JWT (padrão do SecurityConfig), então
 * só cliques de usuários logados são registrados.
 */
@RestController
@RequestMapping("/metricas")
public class AnalyticsController {

    private final AnalyticsService service;

    public AnalyticsController(AnalyticsService service) {
        this.service = service;
    }

    @PostMapping("/evento-clique")
    public ResponseEntity<Void> registrarClique(@RequestBody EventoCliqueDTO dto) {
        try {
            boolean registrado = service.registrarClique(dto);
            // 204 se registrou; 401 se não havia usuário logado.
            return registrado
                    ? ResponseEntity.noContent().build()
                    : ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            // Analytics nunca deve atrapalhar o usuário: devolve 204 em caso de falha.
            return ResponseEntity.noContent().build();
        }
    }

    @GetMapping("/endpoints-mais-usados")
    public ResponseEntity<?> endpointsMaisUsados() {
        try {
            return ResponseEntity.ok(service.rankingEndpoints());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao gerar ranking de endpoints");
        }
    }

    @GetMapping("/cliques-mais-usados")
    public ResponseEntity<?> cliquesMaisUsados() {
        try {
            return ResponseEntity.ok(service.rankingCliques());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao gerar ranking de cliques");
        }
    }

    @GetMapping("/mapa-calor")
    public ResponseEntity<?> mapaCalor() {
        try {
            return ResponseEntity.ok(service.mapaCalorAcessos());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao gerar mapa de calor");
        }
    }
}
