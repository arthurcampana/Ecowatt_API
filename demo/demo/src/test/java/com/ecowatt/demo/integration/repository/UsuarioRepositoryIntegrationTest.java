package com.ecowatt.demo.integration.repository;

import com.ecowatt.demo.model.Usuario;
import com.ecowatt.demo.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
class UsuarioRepositoryIntegrationTest {

    @Autowired
    private UsuarioRepository repository;

    private Usuario usuario(String email) {
        return new Usuario(null, "Usuario", "hash", email, LocalDate.now());
    }

    @Test
    @DisplayName("findByEmail deve encontrar o usuario persistido")
    void findByEmail_quandoExiste_deveRetornarUsuario() {
        Usuario salvo = repository.saveAndFlush(usuario("usuario.repo@exemplo.test"));

        Usuario encontrado = repository.findByEmail("usuario.repo@exemplo.test").orElseThrow();

        assertThat(encontrado.getId()).isEqualTo(salvo.getId());
        assertThat(encontrado.getNome()).isEqualTo("Usuario");
    }

    @Test
    @DisplayName("findByEmail deve retornar vazio para email inexistente")
    void findByEmail_quandoInexistente_deveRetornarVazio() {
        assertThat(repository.findByEmail("nao.existe@exemplo.test")).isEmpty();
    }

    @Test
    @DisplayName("email deve ser unico no banco")
    void salvar_quandoEmailDuplicado_deveViolarRestricao() {
        repository.saveAndFlush(usuario("duplicado@exemplo.test"));

        assertThatThrownBy(() -> repository.saveAndFlush(usuario("duplicado@exemplo.test")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
