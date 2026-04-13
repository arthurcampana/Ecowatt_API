package com.ecowatt.demo.controller;

import com.ecowatt.demo.model.Configuracao;
import com.ecowatt.demo.model.Consumo;
import com.ecowatt.demo.model.Usuario;
import com.ecowatt.demo.service.ConfiguracaoService;
import com.ecowatt.demo.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/configuracao")
public class ConfiguracaoController {

    private final ConfiguracaoService service;

    //🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥
    public ConfiguracaoController(ConfiguracaoService configuracaoService){
        this.service = configuracaoService; }
    //🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥

    @PostMapping("/add")
    public ResponseEntity<?> cadastrar(@RequestBody Configuracao config){
        try{
            config = service.cadastrarConfig(config);
            return ResponseEntity.status(HttpStatus.CREATED).body(config);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao cadastrar usuario: " + e.getMessage());
        }
    }

    @GetMapping("/listar")
    public ResponseEntity<?> listar() {
        try {
            List<Configuracao> lista = service.listarConfig();
            return ResponseEntity.ok(lista);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao listar consumos");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscar(@PathVariable Long id) {
        try {
            return service.buscarConfig(id)
                    .<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body("Consumo não encontrado"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao buscar consumo");
        }
    }

    @PutMapping("/alterar/{id}")
    public ResponseEntity<?> alterarConfig(@PathVariable Long id, @RequestBody Configuracao configAtualizado){
        try {
            Optional<Configuracao> config = service.alterarConfig(id, configAtualizado);

            if(config.isPresent())
                return ResponseEntity.ok(config.get());

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Usuario não encontrado");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao atualizar usuario: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        try {
            boolean removido = service.excluirConfig(id);

            if (removido)
                return ResponseEntity.ok("Removido com sucesso");

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Erro: Sonic é guloso demais!");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao deletar consumo");
        }
    }
}



