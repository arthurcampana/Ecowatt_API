package com.ecowatt.demo.repository;

import com.ecowatt.demo.model.EquipamentoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EquipamentoUsuarioRepository extends JpaRepository<EquipamentoUsuario, Long> {

    Optional<EquipamentoUsuario> findByusuario_id(Long EquipamentousuarioId);
    List<EquipamentoUsuario> findAllByusuario_id(Long UsuarioId);
}