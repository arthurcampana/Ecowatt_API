package com.ecowatt.demo.controller;

import com.ecowatt.demo.dto.*;
import com.ecowatt.demo.service.UsuarioService;
import com.ecowatt.demo.service.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.dao.DataIntegrityViolationException;


import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/usuario")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final AuthenticationService authenticationService;

    public UsuarioController(UsuarioService usuarioService, AuthenticationService authenticationService) {
        this.usuarioService = usuarioService;
        this.authenticationService = authenticationService;
    }


    @PostMapping("/add")
    public ResponseEntity<?> cadastrar(@Valid @RequestBody UsuarioRequestDTO dto){
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(usuarioService.cadastrarUsuario(dto));
    
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Email já cadastrado");
    
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno");
        }
    }

    @GetMapping("/buscar/{id}")
    public ResponseEntity<?> buscar(@PathVariable Long id){
        Optional<UsuarioResponseDTO> usuario = usuarioService.buscarUsuario(id);

        return usuario.<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Usuario não encontrado"));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<UsuarioResponseDTO>> listar(){
        return ResponseEntity.ok(usuarioService.listarUsuarios());
    }

    @PutMapping("/alterar/{id}")
    public ResponseEntity<?> alterar(
            @PathVariable Long id,
            @RequestBody UsuarioUpdateDTO dto){

        Optional<UsuarioResponseDTO> usuario = usuarioService.alterarUsuario(id, dto);

        return usuario.<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Usuario não encontrado"));
    }

    @DeleteMapping("/excluir/{id}")
    public ResponseEntity<?> excluir(@PathVariable Long id){
        if(usuarioService.excluirUsuario(id)){
            return ResponseEntity.ok("Removido");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Usuario não encontrado");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDTO dto){

        try{

            return ResponseEntity.ok(
                    authenticationService.autenticar(dto)
            );

        }catch (Exception e){

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Email ou senha inválidos");
        }
    }


}