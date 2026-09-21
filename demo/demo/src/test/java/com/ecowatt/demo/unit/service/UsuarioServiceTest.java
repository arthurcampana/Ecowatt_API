package com.ecowatt.demo.unit.service;

import com.ecowatt.demo.dto.LoginDTO;
import com.ecowatt.demo.dto.LoginResponseDTO;
import com.ecowatt.demo.dto.UsuarioRequestDTO;
import com.ecowatt.demo.dto.UsuarioResponseDTO;
import com.ecowatt.demo.dto.UsuarioUpdateDTO;
import com.ecowatt.demo.model.Usuario;
import com.ecowatt.demo.repository.UsuarioRepository;
import com.ecowatt.demo.service.JwtService;
import com.ecowatt.demo.service.UsuarioService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuarioExistente() {
        return new Usuario(1L, "Usuario A", "hash-senha", "usuario.a@exemplo.test", LocalDate.now());
    }

    @Test
    @DisplayName("cadastrarUsuario deve codificar a senha e nao expor a senha no retorno")
    void cadastrarUsuario_quandoDadosValidos_deveCodificarSenha() {
        UsuarioRequestDTO dto = new UsuarioRequestDTO("Usuario A", "usuario.a@exemplo.test", "senha123");
        when(passwordEncoder.encode("senha123")).thenReturn("hash-senha");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });

        UsuarioResponseDTO resposta = usuarioService.cadastrarUsuario(dto);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertThat(captor.getValue().getSenha()).isEqualTo("hash-senha");
        assertThat(captor.getValue().getSenha()).isNotEqualTo("senha123");
        assertThat(captor.getValue().getDataRegistro()).isEqualTo(LocalDate.now());
        assertThat(resposta.id()).isEqualTo(1L);
        assertThat(resposta.email()).isEqualTo("usuario.a@exemplo.test");
    }

    @Test
    @DisplayName("login deve retornar token com prefixo Bearer para credenciais validas")
    void login_quandoCredenciaisValidas_deveRetornarTokenBearer() {
        Usuario usuario = usuarioExistente();
        LoginDTO dto = new LoginDTO("usuario.a@exemplo.test", "senha123");
        when(usuarioRepository.findByEmail("usuario.a@exemplo.test")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senha123", "hash-senha")).thenReturn(true);
        when(jwtService.gerarToken(usuario)).thenReturn("token-jwt");

        LoginResponseDTO resposta = usuarioService.login(dto);

        assertThat(resposta.token()).isEqualTo("Bearer token-jwt");
        assertThat(resposta.email()).isEqualTo("usuario.a@exemplo.test");
    }

    @Test
    @DisplayName("login deve falhar quando o email nao existe")
    void login_quandoEmailInexistente_deveLancarExcecao() {
        LoginDTO dto = new LoginDTO("inexistente@exemplo.test", "senha123");
        when(usuarioRepository.findByEmail("inexistente@exemplo.test")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.login(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Credenciais inválidas");
        verify(jwtService, never()).gerarToken(any());
    }

    @Test
    @DisplayName("login deve falhar quando a senha esta incorreta")
    void login_quandoSenhaIncorreta_deveLancarExcecao() {
        Usuario usuario = usuarioExistente();
        LoginDTO dto = new LoginDTO("usuario.a@exemplo.test", "errada");
        when(usuarioRepository.findByEmail("usuario.a@exemplo.test")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("errada", "hash-senha")).thenReturn(false);

        assertThatThrownBy(() -> usuarioService.login(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Credenciais inválidas");
        verify(jwtService, never()).gerarToken(any());
    }

    @Test
    @DisplayName("excluirUsuario deve remover quando o id existe")
    void excluirUsuario_quandoIdExiste_deveRetornarTrue() {
        when(usuarioRepository.existsById(1L)).thenReturn(true);

        boolean resultado = usuarioService.excluirUsuario(1L);

        assertThat(resultado).isTrue();
        verify(usuarioRepository).deleteById(1L);
    }

    @Test
    @DisplayName("excluirUsuario deve retornar false quando o id nao existe")
    void excluirUsuario_quandoIdInexistente_deveRetornarFalse() {
        when(usuarioRepository.existsById(99L)).thenReturn(false);

        boolean resultado = usuarioService.excluirUsuario(99L);

        assertThat(resultado).isFalse();
        verify(usuarioRepository, never()).deleteById(99L);
    }

    @Test
    @DisplayName("REGRESSAO: alterar apenas o nome nao deve gravar o nome como senha")
    void alterarUsuario_quandoAlteraNome_naoDeveUsarNomeComoSenha() {
        Usuario usuario = usuarioExistente();
        String hashOriginal = usuario.getSenha();
        UsuarioUpdateDTO dto = new UsuarioUpdateDTO("Novo Nome", null);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        usuarioService.alterarUsuario(1L, dto);

        // Comportamento correto esperado: a senha nao deve mudar ao alterar somente o nome.
        // Este teste deve FALHAR com o codigo atual, pois o servico codifica o nome como senha.
        assertThat(usuario.getSenha())
                .as("A senha nao deveria ser alterada ao mudar apenas o nome")
                .isEqualTo(hashOriginal);
    }
}
