package com.gymmanager.gym_manager.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gymmanager.gym_manager.entity.MetodoDePago;

public interface MetodoDePagoRepository extends JpaRepository<MetodoDePago , Integer> {
    boolean existsByNombre(String nombre);
    Optional<MetodoDePago> findByNombre(String nombre);
}
