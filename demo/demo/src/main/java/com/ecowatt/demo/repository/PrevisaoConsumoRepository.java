package com.ecowatt.demo.repository;

import com.ecowatt.demo.model.PrevisaoConsumo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrevisaoConsumoRepository extends JpaRepository<PrevisaoConsumo, Long> {

    List<PrevisaoConsumo> findByUsuarioIdOrderByAnoMes(Long usuarioId);
}
