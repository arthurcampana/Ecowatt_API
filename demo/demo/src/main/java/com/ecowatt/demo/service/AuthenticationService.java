package com.ecowatt.demo.service;

import com.ecowatt.demo.dto.LoginDTO;
import com.ecowatt.demo.dto.LoginResponseDTO;
import com.ecowatt.demo.model.Usuario;
import com.ecowatt.demo.repository.UsuarioRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    public AuthenticationService(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            UsuarioRepository usuarioRepository
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    public LoginResponseDTO autenticar(LoginDTO dto){

        Authentication authentication =
                authenticationManager.authenticate(

                        new UsernamePasswordAuthenticationToken(
                                dto.email(),
                                dto.senha()
                        )

                );

        UserDetails user =
                (UserDetails) authentication.getPrincipal();

        Usuario usuario =
                usuarioRepository.findByEmail(user.getUsername())
                        .orElseThrow();

        String token =
                jwtService.gerarToken(usuario);

        return new LoginResponseDTO(

                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                "Bearer " + token

        );

    }

}
