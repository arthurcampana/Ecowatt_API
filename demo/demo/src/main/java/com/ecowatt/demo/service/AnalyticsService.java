package com.ecowatt.demo.service;

import com.ecowatt.demo.dto.CliqueRankingDTO;
import com.ecowatt.demo.dto.EndpointUsoDTO;
import com.ecowatt.demo.dto.EventoCliqueDTO;
import com.ecowatt.demo.dto.MapaCalorAcessoDTO;
import com.ecowatt.demo.model.EventoUso;
import com.ecowatt.demo.model.Usuario;
import com.ecowatt.demo.repository.EventoUsoRepository;
import com.ecowatt.demo.repository.UsuarioRepository;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Registra cliques de interface e lê os eventos de uso para gerar as visões
 * de analytics: ranking de endpoints, ranking de cliques e mapa de calor.
 */
@Service
public class AnalyticsService {

    private final EventoUsoRepository eventoUsoRepository;
    private final UsuarioRepository usuarioRepository;

    public AnalyticsService(EventoUsoRepository eventoUsoRepository,
                            UsuarioRepository usuarioRepository) {
        this.eventoUsoRepository = eventoUsoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Registra um clique de interface (camada bronze: grava cru, sem filtrar).
     * Só registra se houver usuário autenticado; cliques anônimos são ignorados.
     *
     * @return true se registrou, false se não havia usuário logado.
     */
    public boolean registrarClique(EventoCliqueDTO dto) {
        Usuario usuario = usuarioLogado();
        if (usuario == null) {
            return false;
        }

        String rotulo = dto.rotulo() == null ? "" : dto.rotulo().trim();

        EventoUso evento = EventoUso.clique(
                usuario,
                rotulo,
                dto.pagina(),
                LocalDateTime.now()
        );

        eventoUsoRepository.save(evento);
        return true;
    }

    /** Endpoints de API mais acessados. */
    public List<EndpointUsoDTO> rankingEndpoints() {
        return eventoUsoRepository.rankingEndpoints()
                .stream()
                .map(p -> new EndpointUsoDTO(p.getEndpoint(), p.getMetodo(), p.getTotal()))
                .toList();
    }

    /** Ranking de cliques de interface (quantos clicaram em sair, config, etc.). */
    public List<CliqueRankingDTO> rankingCliques() {
        return eventoUsoRepository.rankingCliques()
                .stream()
                .map(p -> new CliqueRankingDTO(p.getRotulo(), p.getTotal()))
                .toList();
    }

    /** Mapa de calor: acessos por (dia da semana x hora do dia). */
    public List<MapaCalorAcessoDTO> mapaCalorAcessos() {
        return eventoUsoRepository.mapaCalorAcessos()
                .stream()
                .map(p -> new MapaCalorAcessoDTO(p.getDiaSemana(), p.getHora(), p.getTotal()))
                .toList();
    }

    /** Resolve o usuário autenticado a partir do e-mail no SecurityContext. */
    private Usuario usuarioLogado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            return null;
        }
        String email = auth.getName();
        if (email.isBlank() || "anonymousUser".equals(email)) {
            return null;
        }
        return usuarioRepository.findByEmail(email).orElse(null);
    }
}
