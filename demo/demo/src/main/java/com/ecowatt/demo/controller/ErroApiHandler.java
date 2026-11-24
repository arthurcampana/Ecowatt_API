package com.ecowatt.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Respostas de erro da API no formato {"erro": "..."}, que o frontend ja
 * exibe (apiClient.js le o campo "erro").
 *
 *  - MethodArgumentNotValidException (400): falhou alguma regra do DTO
 *    (@Valid), ex.: consumo negativo ou senha curta. Devolve a mensagem da regra.
 *  - HttpMessageNotReadableException (400): o JSON nao pode ser lido, ex.: data
 *    em formato invalido ou numero escrito como texto.
 *  - AccessDeniedException (403): o usuario logado tentou acessar dados de
 *    outro usuario ou uma lista exclusiva do ADMIN (ver config/ControleAcesso).
 *    E 403, e nao 401, para o frontend nao tratar como sessao expirada.
 */
@RestControllerAdvice
public class ErroApiHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> dadosInvalidos(MethodArgumentNotValidException e) {
        String mensagem = e.getBindingResult().getFieldErrors().stream()
                .map(erro -> erro.getDefaultMessage())
                .findFirst()
                .orElse("Dados inválidos");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("erro", mensagem));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> corpoIlegivel(HttpMessageNotReadableException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("erro", "Formato dos dados inválido. Confira datas e números."));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> acessoNegado(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("erro", "Acesso negado: você não tem permissão para acessar estes dados."));
    }
}
