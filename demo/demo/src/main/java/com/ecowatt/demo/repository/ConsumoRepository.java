package com.ecowatt.demo.repository;

import com.ecowatt.demo.model.Consumo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConsumoRepository extends JpaRepository<Consumo, Long> {

    List<Consumo> findByUsuarioId(Long usuarioId);
}