package com.ecowatt.demo.repository;

import com.ecowatt.demo.model.EventoUso;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EventoUsoRepository extends JpaRepository<EventoUso, Long> {

    /** Conta eventos por tipo ("API" ou "CLIQUE"), usado para evitar duplicar dados de demo. */
    long countByTipoEvento(String tipoEvento);

    /** Remove todos os eventos de um tipo (usado para regerar os dados de demo de clique). */
    @Modifying
    @Transactional
    long deleteByTipoEvento(String tipoEvento);

    /**
     * Ranking de endpoints mais usados: agrupa os eventos por endpoint + método
     * e conta quantas vezes cada um foi acessado, do mais usado para o menos usado.
     */
    @Query(value = """
            SELECT
                e.endpoint        AS endpoint,
                e.metodo          AS metodo,
                COUNT(*)          AS total
            FROM evento_uso e
            WHERE e.tipo_evento = 'API'
            GROUP BY e.endpoint, e.metodo
            ORDER BY total DESC
            """, nativeQuery = true)
    List<EndpointUsoProjection> rankingEndpoints();

    /**
     * Mapa de calor de acesso: para cada combinação (dia da semana x hora do dia),
     * conta quantos acessos aconteceram. EXTRACT(DOW) no PostgreSQL vai de
     * 0 (domingo) a 6 (sábado); EXTRACT(HOUR) vai de 0 a 23.
     */
    @Query(value = """
            SELECT
                CAST(EXTRACT(DOW  FROM e.data_hora) AS int) AS diaSemana,
                CAST(EXTRACT(HOUR FROM e.data_hora) AS int) AS hora,
                COUNT(*)                                    AS total
            FROM evento_uso e
            GROUP BY diaSemana, hora
            ORDER BY diaSemana, hora
            """, nativeQuery = true)
    List<MapaCalorProjection> mapaCalorAcessos();

    /**
     * Ranking de cliques de interface: agrupa os eventos do tipo CLIQUE pelo
     * rótulo do elemento e conta quantos cliques cada um recebeu.
     * Responde "quantos clicaram em sair, config, etc.".
     */
    @Query(value = """
            SELECT
                e.rotulo   AS rotulo,
                COUNT(*)   AS total
            FROM evento_uso e
            WHERE e.tipo_evento = 'CLIQUE'
              AND e.rotulo IS NOT NULL
            GROUP BY e.rotulo
            ORDER BY total DESC
            """, nativeQuery = true)
    List<CliqueRankingProjection> rankingCliques();

    /**
     * Ranking de cliques agrupado por página e rótulo: para cada rota do front,
     * conta quantos cliques cada elemento recebeu. Permite montar uma tabela
     * separada por página (onde o usuário clica dentro de cada tela).
     * Ordena por página e, dentro dela, do elemento mais clicado para o menos.
     */
    @Query(value = """
            SELECT
                COALESCE(e.pagina, '(sem pagina)') AS pagina,
                e.rotulo                           AS rotulo,
                COUNT(*)                           AS total
            FROM evento_uso e
            WHERE e.tipo_evento = 'CLIQUE'
              AND e.rotulo IS NOT NULL
            GROUP BY e.pagina, e.rotulo
            ORDER BY pagina ASC, total DESC
            """, nativeQuery = true)
    List<CliquePorPaginaProjection> rankingCliquesPorPagina();

    /** Projeção para o ranking de cliques por página. */
    interface CliquePorPaginaProjection {
        String getPagina();
        String getRotulo();
        Long getTotal();
    }

    /** Projeção para o ranking de endpoints. */
    interface EndpointUsoProjection {
        String getEndpoint();
        String getMetodo();
        Long getTotal();
    }

    /** Projeção para o ranking de cliques. */
    interface CliqueRankingProjection {
        String getRotulo();
        Long getTotal();
    }

    /** Projeção para o mapa de calor (dia da semana x hora). */
    interface MapaCalorProjection {
        Integer getDiaSemana();
        Integer getHora();
        Long getTotal();
    }
}
