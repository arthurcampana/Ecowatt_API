package com.ecowatt.demo.controller;

import com.ecowatt.demo.dto.*;
import com.ecowatt.demo.service.ConfiguracaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/configuracao")
public class ConfiguracaoController {

    private final ConfiguracaoService service;


    public ConfiguracaoController(ConfiguracaoService configuracaoService){
        this.service = configuracaoService; }


    @PreAuthorize("@acesso.podeAcessarUsuario(#dto.usuarioId())")
    @PostMapping("/add")
    public ResponseEntity<ConfiguracaoResponseDTO> salvar(
            @Valid @RequestBody ConfiguracaoRequestDTO dto
    ) {

        try {

            ConfiguracaoResponseDTO novo = service.salvar(dto);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(novo);

        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .build();
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/listar")
    public ResponseEntity<?> listar() {
        try {
            List<ConfiguracaoResponseDTO> lista = service.listarConfig();
            return ResponseEntity.ok(lista);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao listar configurações");
        }
    }

    @PreAuthorize("@acesso.donoDaConfiguracao(#id)")
    @GetMapping("/{id}")
    public ResponseEntity<?> buscar(@PathVariable Long id) {
        try {
            return service.buscarConfig(id)
                    .<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body("Configuração não encontrada"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao buscar configuração");
        }
    }

    @PreAuthorize("@acesso.donoDaConfiguracao(#id)")
    @PutMapping("/alterar/{id}")
    public ResponseEntity<?> alterarConfig(
            @PathVariable Long id,
            @Valid @RequestBody ConfiguracaoUpdateDTO configAtualizado){
        try {
            Optional<ConfiguracaoResponseDTO> config = service.alterarConfig(id, configAtualizado);

            if(config.isPresent())
                return ResponseEntity.ok(config.get());

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Configuração não encontrada");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao atualizar configuração");
        }
    }

    @PreAuthorize("@acesso.donoDaConfiguracao(#id)")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        try {
            boolean removido = service.excluirConfig(id);

            if (removido)
                return ResponseEntity.ok("Removido com sucesso");

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Configuração não encontrada");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao excluir configuração");
        }
    }
    @PreAuthorize("@acesso.podeAcessarUsuario(#id)")
    @GetMapping("/usuario/{id}")
    public ResponseEntity<?> buscarPorUsuario(
            @PathVariable Long id
    ){

        try{

            return service.buscarPorUsuario(id)
                    .<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body("Configuração não encontrada"));

        }catch(Exception e){

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao buscar configuração");
        }
    }
}
