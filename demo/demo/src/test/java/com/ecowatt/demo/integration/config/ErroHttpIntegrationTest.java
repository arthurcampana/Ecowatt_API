package com.ecowatt.demo.integration.config;

import com.ecowatt.demo.model.Usuario;
import com.ecowatt.demo.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Um usuario logado que envia dados invalidos ou acessa uma rota inexistente
 * deve receber 400/404, e nao 401. O frontend trata todo 401 como sessao
 * expirada e faz logout, entao um 401 indevido desloga o usuario.
 *
 * Usa um servidor real (RANDOM_PORT): o redirecionamento interno do Spring
 * Boot para /error, que causava o 401, nao acontece no MockMvc.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ErroHttpIntegrationTest {

    @Autowired
    private TestRestTemplate http;

    @Autowired
    private JwtService jwtService;

    private String token;

    @BeforeEach
    void gerarToken() {
        // O JwtFilter so valida a assinatura do token; nao consulta o banco.
        Usuario usuario = new Usuario(
                1L, "Teste", "senha", "teste-erro@ecowatt.test", LocalDate.now(), "USER");
        token = "Bearer " + jwtService.gerarToken(usuario);
    }

    private ResponseEntity<String> enviar(HttpMethod metodo, String rota, String json, boolean autenticado) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (autenticado) {
            headers.set(HttpHeaders.AUTHORIZATION, token);
        }
        return http.exchange(rota, metodo, new HttpEntity<>(json, headers), String.class);
    }

    @Test
    void consumoNegativoRetorna400ComMensagem() {
        ResponseEntity<String> resposta = enviar(HttpMethod.POST, "/consumo/add",
                "{\"usuarioId\":1,\"consumoKwh\":-5}", true);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resposta.getBody()).contains("Consumo não pode ser negativo");
    }

    @Test
    void dataEmFormatoInvalidoRetorna400() {
        ResponseEntity<String> resposta = enviar(HttpMethod.POST, "/consumo/add",
                "{\"usuarioId\":1,\"consumoKwh\":10,\"dataRegistro\":\"ontem\"}", true);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resposta.getBody()).contains("\"erro\"");
    }

    @Test
    void senhaCurtaNoPerfilRetorna400ComMensagem() {
        ResponseEntity<String> resposta = enviar(HttpMethod.PUT, "/usuario/alterar/1",
                "{\"nome\":\"Teste\",\"senha\":\"123\"}", true);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resposta.getBody()).contains("Senha deve ter no mínimo 6 caracteres");
    }

    @Test
    void rotaInexistenteComTokenRetorna404() {
        ResponseEntity<String> resposta = enviar(HttpMethod.GET, "/rota-que-nao-existe", null, true);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void semTokenContinuaRetornando401() {
        ResponseEntity<String> resposta = enviar(HttpMethod.GET, "/consumo/listar", null, false);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
