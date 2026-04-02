package com.ecowatt.demo.service;

import com.ecowatt.demo.model.Cliente;

import java.util.ArrayList;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ClienteService {

    private ArrayList<Cliente> clientes = new ArrayList<>();

    public Cliente cadastrarCliente(Cliente cliente){
        clientes.add(cliente);
        return cliente;
    }

    public ArrayList listarClientes(){
        return clientes;
    }

    public Optional<Cliente> buscarCliente(Long cliente_id){
        return clientes.stream()
                .filter(p -> p.getId_cliente().equals(cliente_id))
                .findFirst();
    }

    public Optional<Cliente> alterarCliente(Long codigo, Cliente clienteAtualizado){
        for (Cliente cliente : clientes) {
            if (cliente.getId_cliente().equals(codigo)) {
                cliente.setNome(clienteAtualizado.getNome());
                cliente.setSenha(clienteAtualizado.getSenha());
                cliente.setEmail(clienteAtualizado.getEmail());
                return Optional.ofNullable(cliente);
            }
        }

        return Optional.empty();
    }

    public boolean excluirCliente(Long cliente_id){
        return clientes.removeIf(c -> c.getId_cliente().equals(cliente_id));

    }
}
