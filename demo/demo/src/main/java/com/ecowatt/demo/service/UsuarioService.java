package com.ecowatt.demo.service;

import com.ecowatt.demo.dto.UsuarioRequestDTO;
import com.ecowatt.demo.dto.UsuarioResponseDTO;
import com.ecowatt.demo.dto.UsuarioUpdateDTO;
import com.ecowatt.demo.dto.LoginDTO;
import com.ecowatt.demo.model.Usuario;
import com.ecowatt.demo.repository.UsuarioRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder){
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // CREATE
    public UsuarioResponseDTO cadastrarUsuario(UsuarioRequestDTO dto){
        Usuario usuario = new Usuario();

        usuario.setNome(dto.nome());
        usuario.setEmail(dto.email());
        usuario.setSenha(passwordEncoder.encode(dto.senha()));
        usuario.setDataRegistro(LocalDate.now());

        usuario = usuarioRepository.save(usuario);

        return new UsuarioResponseDTO(usuario);
    }

    // READ ALL
    public List<UsuarioResponseDTO> listarUsuarios(){
        return usuarioRepository.findAll()
                .stream()
                .map(UsuarioResponseDTO::new)
                .collect(Collectors.toList());
    }

    // READ ONE
    public Optional<UsuarioResponseDTO> buscarUsuario(Long id){
        return usuarioRepository.findById(id)
                .map(UsuarioResponseDTO::new);
    }

    // UPDATE
    public Optional<UsuarioResponseDTO> alterarUsuario(Long id, UsuarioUpdateDTO dto){
        return usuarioRepository.findById(id).map(usuario -> {

            if(dto.nome() != null){
                usuario.setNome(dto.nome());
            }

            if(dto.nome() != null){
                usuario.setSenha(passwordEncoder.encode(dto.nome()));
            }

            usuarioRepository.save(usuario);

            return new UsuarioResponseDTO(usuario);
        });
    }

    // DELETE
    public boolean excluirUsuario(Long id){
        if(usuarioRepository.existsById(id)){
            usuarioRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public UsuarioResponseDTO login(LoginDTO dto){

        Usuario usuario = usuarioRepository.findByEmail(dto.email())
                .orElseThrow(() ->
                        new RuntimeException("Credenciais inválidas"));

        boolean senhaValida = passwordEncoder.matches(
                dto.senha(),
                usuario.getSenha()
        );

        if(!senhaValida){
            throw new RuntimeException("Credenciais inválidas");
        }

        return new UsuarioResponseDTO(usuario);
    }
}