package com.ecowatt.demo.unit.service;

import com.ecowatt.demo.dto.LoginDTO;
import com.ecowatt.demo.dto.LoginResponseDTO;
import com.ecowatt.demo.model.Usuario;
import com.ecowatt.demo.repository.UsuarioRepository;
import com.ecowatt.demo.service.AuthenticationService;
import com.ecowatt.demo.service.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthenticationService service;

    private Usuario usuario() {
        return new Usuario(1L, "Usuario A", "hash", "usuario.a@exemplo.test", LocalDate.now());
    }

    @Test
    @DisplayName("autenticar deve enviar email e senha ao manager e retornar token Bearer")
    void autenticar_quandoCredenciaisValidas_deveRetornarToken() {
        LoginDTO dto = new LoginDTO("usuario.a@exemplo.test", "senha123");
        UserDetails principal = User.withUsername(dto.email()).password("hash").roles("USER").build();
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(usuarioRepository.findByEmail(dto.email())).thenReturn(Optional.of(usuario()));
        when(jwtService.gerarToken(any(Usuario.class))).thenReturn("token-jwt");

        LoginResponseDTO resposta = service.autenticar(dto);

        ArgumentCaptor<UsernamePasswordAuthenticationToken> captor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(captor.capture());
        assertThat(captor.getValue().getPrincipal()).isEqualTo(dto.email());
        assertThat(captor.getValue().getCredentials()).isEqualTo("senha123");
        assertThat(resposta.token()).isEqualTo("Bearer token-jwt");
        assertThat(resposta.id()).isEqualTo(1L);
    }

    @Test
    @DisplayName("autenticar deve propagar credenciais invalidas")
    void autenticar_quandoCredenciaisInvalidas_devePropagarExcecao() {
        LoginDTO dto = new LoginDTO("usuario.a@exemplo.test", "errada");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Credenciais inválidas"));

        assertThatThrownBy(() -> service.autenticar(dto))
                .isInstanceOf(BadCredentialsException.class);
        verify(usuarioRepository, never()).findByEmail(any());
        verify(jwtService, never()).gerarToken(any());
    }

    @Test
    @DisplayName("autenticar deve falhar se usuario autenticado nao estiver mais no repositorio")
    void autenticar_quandoUsuarioNaoEncontradoAposAutenticacao_deveFalhar() {
        LoginDTO dto = new LoginDTO("usuario.a@exemplo.test", "senha123");
        UserDetails principal = User.withUsername(dto.email()).password("hash").roles("USER").build();
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(usuarioRepository.findByEmail(dto.email())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.autenticar(dto))
                .isInstanceOf(NoSuchElementException.class);
        verify(jwtService, never()).gerarToken(any());
    }
}
