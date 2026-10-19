package com.ecowatt.demo.interceptor;

import com.ecowatt.demo.model.EventoUso;
import com.ecowatt.demo.model.Usuario;
import com.ecowatt.demo.repository.EventoUsoRepository;
import com.ecowatt.demo.repository.UsuarioRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;

/**
 * Intercepta TODA requisição (após o controller responder) e registra um
 * EventoUso. É assim que coletamos "onde os usuários mais usam" sem precisar
 * sujar cada controller com código de log.
 *
 * O usuário é resolvido pelo e-mail que o JwtFilter coloca no SecurityContext.
 * Requisições anônimas (login, cadastro) ficam com usuario = null.
 */
@Component
public class EventoUsoInterceptor implements HandlerInterceptor {

    private final EventoUsoRepository eventoUsoRepository;
    private final UsuarioRepository usuarioRepository;

    public EventoUsoInterceptor(EventoUsoRepository eventoUsoRepository,
                                UsuarioRepository usuarioRepository) {
        this.eventoUsoRepository = eventoUsoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        try {
            Usuario usuario = resolverUsuario();

            EventoUso evento = new EventoUso(
                    usuario,
                    request.getRequestURI(),
                    request.getMethod(),
                    response.getStatus(),
                    LocalDateTime.now()
            );

            eventoUsoRepository.save(evento);
        } catch (Exception e) {
            // Analytics nunca deve quebrar a requisição real do usuário:
            // se o registro falhar, apenas ignoramos.
        }
    }

    private Usuario resolverUsuario() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || auth.getPrincipal() == null) {
            return null;
        }

        // O JwtFilter usa o e-mail como principal.
        String email = auth.getName();

        if (email == null || email.isBlank() || "anonymousUser".equals(email)) {
            return null;
        }

        return usuarioRepository.findByEmail(email).orElse(null);
    }
}
