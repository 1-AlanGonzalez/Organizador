package com.gymmanager.gym_manager.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gymmanager.gym_manager.entity.ConfiguracionDePago;
import com.gymmanager.gym_manager.entity.MetodoDePago;

public interface ConfiguracionPagoRepository extends JpaRepository<ConfiguracionDePago, Integer> {
    Optional<ConfiguracionDePago> findByMetodoDePagoAndActivoTrue(MetodoDePago metodoDePago);
}
