package com.ecowatt.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.Customizer;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

@Configuration
// Habilita @PreAuthorize nos controllers (verificacao de dono do dado, ver
// ControleAcesso).
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    public SecurityConfig(JwtFilter jwtFilter, JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint){
        this.jwtFilter = jwtFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    }

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .cors(Customizer.withDefaults())

                .csrf(csrf -> csrf.disable())

                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .exceptionHandling(e -> e
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler((request, response, ex) -> {
                            // Usuario autenticado, porem sem permissao (ex.: USER
                            // tentando ler analytics): 403, distinto do 401 de sessao
                            // ausente/expirada, para o front nao deslogar o usuario.
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().write("{\"erro\":\"Acesso negado\"}");
                        })
                )

                .authorizeHttpRequests(auth -> auth

                        // Quando uma requisicao falha (400, 404, 500), o Spring
                        // Boot redireciona internamente para /error para montar
                        // a resposta. Sem liberar esse redirecionamento, a
                        // seguranca o bloqueava e todo erro virava 401, o que
                        // fazia o frontend deslogar o usuario. A requisicao
                        // original continua exigindo token normalmente.
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()

                        .requestMatchers(
                                "/usuario/add",
                                "/usuario/login",
                                "/demo/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // Rastreio de cliques: qualquer usuario logado registra cliques.
                        .requestMatchers(HttpMethod.POST, "/metricas/evento-clique")
                        .authenticated()

                        // Leituras de analytics: somente ADMIN.
                        .requestMatchers(HttpMethod.GET,
                                "/metricas/endpoints-mais-usados",
                                "/metricas/cliques-mais-usados",
                                "/metricas/cliques-por-pagina",
                                "/metricas/mapa-calor"
                        ).hasRole("ADMIN")

                        .anyRequest()
                        .authenticated()
                )

                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    // Origens liberadas no CORS. Vem de app.cors.allowed-origins
    // (variavel de ambiente CORS_ALLOWED_ORIGINS), com fallback para os
    // enderecos locais de desenvolvimento definidos no application.properties.
    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins}") List<String> origensPermitidas
    ){

        CorsConfiguration config =
                new CorsConfiguration();

        config.setAllowCredentials(true);

        config.setAllowedOrigins(
                origensPermitidas.stream()
                        .map(String::trim)
                        .filter(origem -> !origem.isEmpty())
                        .toList()
        );

        config.setAllowedHeaders(List.of("*"));

        config.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "DELETE"
        ));

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                config
        );

        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {

        return configuration.getAuthenticationManager();

    }
}
