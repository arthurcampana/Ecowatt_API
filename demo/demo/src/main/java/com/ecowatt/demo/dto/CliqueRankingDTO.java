package com.ecowatt.demo.dto;

/**
 * Uma linha do ranking de cliques: quantas vezes um rótulo foi clicado.
 *
 * @param rotulo identificador do elemento (ex.: "logout").
 * @param total  número de cliques registrados.
 */
public record CliqueRankingDTO(
        String rotulo,
        Long total
) {
}
