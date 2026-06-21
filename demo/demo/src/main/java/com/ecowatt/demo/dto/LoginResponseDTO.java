package com.ecowatt.demo.dto;

public record LoginResponseDTO(
        Long id,
        String nome,
        String email,
        String token
) {}