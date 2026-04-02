package com.ecowatt.demo.service;

import com.ecowatt.demo.model.Usuario;
import com.ecowatt.demo.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository){
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario cadastrarUsuario(Usuario usuario){
        return usuarioRepository.save(usuario);
    }

    public List<Usuario> listarUsuarios(){
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> buscarUsuario(Long id){
        return usuarioRepository.findById(id);
    }

    public Optional<Usuario> alterarUsuario(Long id, Usuario usuarioAtualizado){
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);

        if(usuarioOpt.isPresent()){
            Usuario usuario = usuarioOpt.get();
            usuario.setNome(usuarioAtualizado.getNome());
            usuario.setSenha(usuarioAtualizado.getSenha());
            usuario.setEmail(usuarioAtualizado.getEmail());

            return Optional.of(usuarioRepository.save(usuario));
        }

        return Optional.empty();
    }

    public boolean excluirUsuario(Long id){
        if(usuarioRepository.existsById(id)){
            usuarioRepository.deleteById(id);
            return true;
        }
        return false;
    }
}