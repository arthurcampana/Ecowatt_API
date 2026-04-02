package com.ecowatt.demo.repository;

import com.ecowatt.demo.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
}