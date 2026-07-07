package com.ecowatt.demo.repository;

import com.ecowatt.demo.model.Equipamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EquipamentoRepository extends JpaRepository<Equipamento, Long> {

    Optional<Equipamento> findByNome(String nome);


}