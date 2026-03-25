package com.ecowatt.demo.Service;

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
                .filter(p -> p.getCliente_id().equals(cliente_id))
                .findFirst();
    }

    public Optional<Cliente> alterarCliente(Long codigo, Cliente clienteAtualizado){
        for (Cliente cliente : clientes) {
            if (cliente.getCliente_id().equals(codigo)) {
                cliente.setNome(clienteAtualizado.getNome());
                cliente.setSenha(clienteAtualizado.getSenha());
                cliente.setEmail(clienteAtualizado.getEmail());
                return Optional.ofNullable(cliente);
            }
        }

        return Optional.empty();
    }

    public boolean excluirCliente(Long cliente_id){
        return clientes.removeIf(c -> c.getCliente_id().equals(cliente_id));

    }
}
