package com.ecowatt.demo.integration.config;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Garante que as origens liberadas no CORS vem da configuracao
 * (app.cors.allowed-origins / CORS_ALLOWED_ORIGINS), e nao de uma lista fixa.
 * Usa requisicoes de preflight (OPTIONS), que e o que o navegador envia antes
 * de chamar a API a partir de outro endereco.
 */
class CorsConfigIntegrationTest {

    private static final String ROTA = "/usuario/login";

    @Nested
    @SpringBootTest
    @AutoConfigureMockMvc
    @ActiveProfiles("test")
    class ComOrigensPadrao {

        @Autowired
        private MockMvc mockMvc;

        @Test
        void aceitaFrontendLocal() throws Exception {
            mockMvc.perform(options(ROTA)
                            .header(HttpHeaders.ORIGIN, "http://localhost:5500")
                            .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                    .andExpect(status().isOk())
                    .andExpect(header().string(
                            HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                            "http://localhost:5500"));
        }

        @Test
        void recusaOrigemDesconhecida() throws Exception {
            mockMvc.perform(options(ROTA)
                            .header(HttpHeaders.ORIGIN, "https://site-desconhecido.com")
                            .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @SpringBootTest
    @AutoConfigureMockMvc
    @ActiveProfiles("test")
    @TestPropertySource(properties =
            "app.cors.allowed-origins=https://ecowatt.exemplo.com, https://outro.exemplo.com")
    class ComOrigensConfiguradas {

        @Autowired
        private MockMvc mockMvc;

        @Test
        void aceitaOrigemConfigurada() throws Exception {
            mockMvc.perform(options(ROTA)
                            .header(HttpHeaders.ORIGIN, "https://outro.exemplo.com")
                            .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                    .andExpect(status().isOk())
                    .andExpect(header().string(
                            HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                            "https://outro.exemplo.com"));
        }

        @Test
        void deixaDeAceitarLocalhostQuandoConfigurado() throws Exception {
            mockMvc.perform(options(ROTA)
                            .header(HttpHeaders.ORIGIN, "http://localhost:5500")
                            .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                    .andExpect(status().isForbidden());
        }
    }
}
