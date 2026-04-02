package com.ecowatt.demo.controller;

import com.ecowatt.demo.model.Usuario;
import com.ecowatt.demo.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/usuario")
public class UsuarioController {

    private final UsuarioService usuarioService;

    // 🔥 injeção correta
    public UsuarioController(UsuarioService usuarioService){
        this.usuarioService = usuarioService;
    }

    @PostMapping("/add")
    public ResponseEntity<?> cadastrar(@RequestBody Usuario usuario){
        try{
            usuario = usuarioService.cadastrarUsuario(usuario);
            return ResponseEntity.status(HttpStatus.CREATED).body(usuario);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao cadastrar usuario: " + e.getMessage());
        }
    }

    @GetMapping("/buscar/{id}")
    public ResponseEntity<?> buscarUsuario(@PathVariable Long id){
        try {
            Optional<Usuario> usuario = usuarioService.buscarUsuario(id);
            return usuario.<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body("Usuario não encontrado"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao buscar usuario: " + e.getMessage());
        }
    }

    @GetMapping("/buscar")
    public ResponseEntity<?> listarUsuarios(){
        try {
            List<Usuario> usuarios = usuarioService.listarUsuarios();
            return ResponseEntity.ok(usuarios);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao listar usuarios: " + e.getMessage());
        }
    }

    // 🔥 PUT é mais correto para update
    @PutMapping("/alterar/{id}")
    public ResponseEntity<?> alterarUsuario(@PathVariable Long id, @RequestBody Usuario usuarioAtualizado){
        try {
            Optional<Usuario> usuario = usuarioService.alterarUsuario(id, usuarioAtualizado);

            if(usuario.isPresent())
                return ResponseEntity.ok(usuario.get());

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Usuario não encontrado");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao atualizar usuario: " + e.getMessage());
        }
    }

    @DeleteMapping("/excluir/{id}")
    public ResponseEntity<?> excluir(@PathVariable Long id) {
        try {
            boolean removido = usuarioService.excluirUsuario(id);

            if (removido) {
                return ResponseEntity.ok("Usuario removido com sucesso!");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Usuario não encontrado");
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao excluir usuario: " + e.getMessage());
        }
    }
}