package com.ecowatt.demo.unit.service;

import com.ecowatt.demo.model.Usuario;
import com.ecowatt.demo.repository.UsuarioRepository;
import com.ecowatt.demo.service.UserDetailsServiceCustom;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceCustomTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UserDetailsServiceCustom service;

    @Test
    @DisplayName("carregar usuario deve preservar email, hash e perfil USER")
    void loadUserByUsername_quandoExiste_deveRetornarUserDetails() {
        Usuario usuario = new Usuario(1L, "Usuario A", "hash-senha",
                "usuario.a@exemplo.test", LocalDate.now());
        when(usuarioRepository.findByEmail("usuario.a@exemplo.test"))
                .thenReturn(Optional.of(usuario));

        UserDetails resultado = service.loadUserByUsername("usuario.a@exemplo.test");

        assertThat(resultado.getUsername()).isEqualTo("usuario.a@exemplo.test");
        assertThat(resultado.getPassword()).isEqualTo("hash-senha");
        assertThat(resultado.getAuthorities()).extracting("authority")
                .containsExactly("ROLE_USER");
        assertThat(resultado.isEnabled()).isTrue();
        assertThat(resultado.isAccountNonExpired()).isTrue();
        assertThat(resultado.isAccountNonLocked()).isTrue();
        assertThat(resultado.isCredentialsNonExpired()).isTrue();
        verify(usuarioRepository).findByEmail("usuario.a@exemplo.test");
    }

    @Test
    @DisplayName("carregar usuario deve lançar excecao quando email nao existe")
    void loadUserByUsername_quandoInexistente_deveLancarExcecao() {
        when(usuarioRepository.findByEmail("inexistente@exemplo.test"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("inexistente@exemplo.test"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Usuário não encontrado");
    }
}
