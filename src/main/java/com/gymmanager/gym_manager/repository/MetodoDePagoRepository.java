package com.gymmanager.gym_manager.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gymmanager.gym_manager.entity.MetodoDePago;
import com.gymmanager.gym_manager.entity.Usuario;

public interface MetodoDePagoRepository extends JpaRepository<MetodoDePago , Integer> {
    List<MetodoDePago> findByUsuario(Usuario usuario);
    Optional<MetodoDePago> findByNombreAndUsuario(String nombre, Usuario usuario);
    Optional<MetodoDePago> findByIdMetodoDePagoAndUsuario(
            Integer idMetodoDePago,
            Usuario usuario
    );
}
