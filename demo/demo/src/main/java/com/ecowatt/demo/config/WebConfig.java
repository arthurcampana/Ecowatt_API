package com.ecowatt.demo.config;

import com.ecowatt.demo.interceptor.EventoUsoInterceptor;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Registra o EventoUsoInterceptor para rodar em todas as requisições,
 * exceto as de documentação (Swagger) e as próprias rotas de analytics,
 * para não poluir as estatísticas com acessos ao painel.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final EventoUsoInterceptor eventoUsoInterceptor;

    public WebConfig(EventoUsoInterceptor eventoUsoInterceptor) {
        this.eventoUsoInterceptor = eventoUsoInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(eventoUsoInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/metricas/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**"
                );
    }
}
