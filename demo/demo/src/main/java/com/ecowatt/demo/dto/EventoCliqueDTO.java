package com.ecowatt.demo.dto;

/**
 * Payload enviado pelo front a cada clique rastreado.
 *
 * @param rotulo identificador do elemento clicado (ex.: "logout", "config",
 *               ou o texto do botão quando não há data-evento). É a camada
 *               bronze: guardamos cru, a padronização acontece depois (PySpark).
 * @param pagina rota do front onde o clique ocorreu (ex.: "/dashboard").
 */
public record EventoCliqueDTO(
        String rotulo,
        String pagina
) {
}
