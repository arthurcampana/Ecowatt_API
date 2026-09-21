package com.ecowatt.demo.unit.service;

import com.ecowatt.demo.model.Usuario;
import com.ecowatt.demo.service.JwtService;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.time.LocalDate;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET =
            "ChaveDeTesteSuperSecretaComMaisDe32Caracteres1234567890";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", SECRET);
    }

    private Usuario usuario() {
        return new Usuario(1L, "Usuario A", "hash", "usuario.a@exemplo.test", LocalDate.now());
    }

    @Test
    @DisplayName("gerarToken deve usar o email como subject e permitir validacao")
    void gerarToken_deveConterEmailNoSubject() {
        String token = jwtService.gerarToken(usuario());

        assertThat(token).isNotBlank();
        assertThat(jwtService.validarToken(token)).isEqualTo("usuario.a@exemplo.test");
    }

    @Test
    @DisplayName("gerarToken deve definir expiracao proxima de uma hora")
    void gerarToken_deveExpirarEmAproximadamenteUmaHora() {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes());
        String token = jwtService.gerarToken(usuario());

        Date expiracao = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration();

        long diferencaMs = expiracao.getTime() - System.currentTimeMillis();
        // Tolerancia de +/- 1 minuto em torno de 1 hora (3.600.000 ms).
        assertThat(diferencaMs).isBetween(3_540_000L, 3_660_000L);
    }

    @Test
    @DisplayName("validarToken deve falhar para token adulterado")
    void validarToken_quandoTokenAdulterado_deveLancarExcecao() {
        String token = jwtService.gerarToken(usuario());
        String adulterado = token.substring(0, token.length() - 2) + "xy";

        assertThatThrownBy(() -> jwtService.validarToken(adulterado))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("validarToken deve falhar para token assinado com outra chave")
    void validarToken_quandoAssinaturaDiferente_deveLancarExcecao() {
        SecretKey outraChave = Keys.hmacShaKeyFor(
                "OutraChaveDiferenteComMaisDe32Caracteres1234567890".getBytes());
        String tokenExterno = Jwts.builder()
                .subject("usuario.a@exemplo.test")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(outraChave)
                .compact();

        assertThatThrownBy(() -> jwtService.validarToken(tokenExterno))
                .isInstanceOf(RuntimeException.class);
    }
}
