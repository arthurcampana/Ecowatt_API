package com.ecowatt.demo.dto;

/**
 * Uma linha do ranking de endpoints mais usados.
 *
 * @param endpoint caminho acessado (ex.: "/consumo/listar").
 * @param metodo   método HTTP (GET, POST, ...).
 * @param total    quantas vezes esse endpoint foi acessado.
 */
public record EndpointUsoDTO(
        String endpoint,
        String metodo,
        Long total
) {
}
