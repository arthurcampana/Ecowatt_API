package com.ecowatt.demo.repository;

import com.ecowatt.demo.model.Equipamento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipamentoRepository extends JpaRepository<Equipamento, Long> {
}