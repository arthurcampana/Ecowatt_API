package com.ecowatt.demo.unit.service;

import com.ecowatt.demo.dto.UsuarioRequestDTO;
import com.ecowatt.demo.dto.UsuarioResponseDTO;
import com.ecowatt.demo.dto.UsuarioUpdateDTO;
import com.ecowatt.demo.model.Usuario;
import com.ecowatt.demo.repository.UsuarioRepository;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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

        // Regressao do defeito ja corrigido: ao alterar apenas o nome, a senha
        // deve permanecer intacta. Antes da correcao o servico gravava o nome
        // como senha e este teste falhava; com a correcao aplicada, ele passa.
        assertThat(usuario.getSenha())
                .as("A senha nao deveria ser alterada ao mudar apenas o nome")
                .isEqualTo(hashOriginal);
    }

    @Test
    @DisplayName("alterarUsuario deve codificar a nova senha quando nome e senha sao informados")
    void alterarUsuario_quandoAlteraNomeESenha_deveCodificarNovaSenha() {
        Usuario usuario = usuarioExistente();
        UsuarioUpdateDTO dto = new UsuarioUpdateDTO("Novo Nome", "novaSenha123");
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode("novaSenha123")).thenReturn("novo-hash");

        Optional<UsuarioResponseDTO> resposta = usuarioService.alterarUsuario(1L, dto);

        assertThat(resposta).isPresent();
        assertThat(usuario.getNome()).isEqualTo("Novo Nome");
        assertThat(usuario.getSenha()).isEqualTo("novo-hash");
        assertThat(usuario.getSenha()).isNotEqualTo("novaSenha123");
        verify(usuarioRepository).save(usuario);
    }

    @Test
    @DisplayName("alterarUsuario deve retornar vazio quando o id nao existe")
    void alterarUsuario_quandoIdInexistente_deveRetornarVazio() {
        UsuarioUpdateDTO dto = new UsuarioUpdateDTO("Qualquer", null);
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<UsuarioResponseDTO> resposta = usuarioService.alterarUsuario(99L, dto);

        assertThat(resposta).isEmpty();
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("listarUsuarios deve converter todos os usuarios para DTO")
    void listarUsuarios_quandoExistemUsuarios_deveRetornarListaDeDTO() {
        Usuario a = new Usuario(1L, "Usuario A", "hash-a", "usuario.a@exemplo.test", LocalDate.now());
        Usuario b = new Usuario(2L, "Usuario B", "hash-b", "usuario.b@exemplo.test", LocalDate.now());
        when(usuarioRepository.findAll()).thenReturn(List.of(a, b));

        List<UsuarioResponseDTO> resposta = usuarioService.listarUsuarios();

        assertThat(resposta).hasSize(2);
        assertThat(resposta.get(0).email()).isEqualTo("usuario.a@exemplo.test");
        assertThat(resposta.get(1).email()).isEqualTo("usuario.b@exemplo.test");
    }
}
