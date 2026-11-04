package com.ecowatt.demo.dto;

/**
 * Uma linha do ranking de cliques agrupado por página: dentro de uma rota do
 * front, quantas vezes um rótulo foi clicado.
 *
 * @param pagina rota do front onde o clique aconteceu (ex.: "/configuracoes").
 * @param rotulo identificador do elemento (ex.: "configuracao_salvar").
 * @param total  número de cliques registrados.
 */
public record CliquePorPaginaDTO(
        String pagina,
        String rotulo,
        Long total
) {
}
