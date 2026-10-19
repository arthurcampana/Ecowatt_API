package com.ecowatt.demo.dto;

/**
 * Uma célula do mapa de calor de acessos (dia da semana x hora do dia).
 *
 * @param diaSemana 0 = domingo ... 6 = sábado (padrão do PostgreSQL EXTRACT(DOW)).
 * @param hora      0 a 23.
 * @param total     número de acessos registrados nessa faixa.
 */
public record MapaCalorAcessoDTO(
        Integer diaSemana,
        Integer hora,
        Long total
) {
}
