package com.ecowatt.demo.controller;

import com.ecowatt.demo.Service.ClienteService;
import com.ecowatt.demo.model.Cliente;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Optional;

@RestController
@RequestMapping("/cliente")
public class ClienteController {

    ClienteService clienteService = new ClienteService();

    public ClienteController(ClienteService cs){
        this.clienteService = cs;
    }

    @PostMapping("/add")
    public ResponseEntity<?> cadastrar(@RequestBody Cliente cliente){
        try{
            cliente = clienteService.cadastrarCliente(cliente);
            return ResponseEntity.status(HttpStatus.CREATED).body(cliente);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao cadastrar cliente: " + e.getMessage());
        }

    }

    @GetMapping("/buscar/{cliente_id}")
    public ResponseEntity<?> buscarCliente(@PathVariable Long codigo){
        try {
            Optional<Cliente> cliente = clienteService.buscarCliente(codigo);
            return cliente.<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body("Cliente não encontrado"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao buscar cliente: " + e.getMessage());
        }
    }

    @GetMapping("/buscar")
    public ResponseEntity<?> listarClientes(){
        try {
            ArrayList<Cliente> clientes = clienteService.listarClientes();
            return ResponseEntity.ok(clientes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao listar clientes: " + e.getMessage());
        }
    }

    @PostMapping("alterar/{codigo}")
    public ResponseEntity<?> alterarCliente(@PathVariable Long cliente_id, @RequestBody Cliente clienteAtualizado){
        try {
            Optional<Cliente> cliente = clienteService.alterarCliente(cliente_id, clienteAtualizado);
            if(cliente.isPresent())
                return ResponseEntity.ok(cliente.get());

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Cliente não encontrado");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao atualizar cliente: " + e.getMessage());
        }

    }

    @DeleteMapping("/excluir/{codigo}")
    public ResponseEntity<?> excluir(@PathVariable Long codigo) {
        try {
            boolean removido = clienteService.excluirCliente(codigo);

            if (removido) {
                return ResponseEntity.ok("Cliente removido com sucesso!");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Cliente não encontrado");
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao excluir cliente: " + e.getMessage());
        }
    }




}
