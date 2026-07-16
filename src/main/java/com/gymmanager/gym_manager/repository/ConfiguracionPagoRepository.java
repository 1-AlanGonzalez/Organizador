package com.gymmanager.gym_manager.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gymmanager.gym_manager.entity.ConfiguracionDePago;
import com.gymmanager.gym_manager.entity.MetodoDePago;
import com.gymmanager.gym_manager.entity.Usuario;

public interface ConfiguracionPagoRepository extends JpaRepository<ConfiguracionDePago, Integer> {

    Optional<ConfiguracionDePago> findByIdAndMetodoDePago_Usuario(Integer id, Usuario usuario);

    Optional<ConfiguracionDePago> findByMetodoDePagoAndActivoTrue(MetodoDePago metodoDePago);
    Optional<ConfiguracionDePago> findByMetodoDePagoAndActivoFalse(MetodoDePago metodoDePago);

    boolean existsByMetodoDePagoAndActivoTrue(MetodoDePago metodoDePago);
    boolean existsByMetodoDePago(MetodoDePago metodoDePago);

    List<ConfiguracionDePago> findByActivoFalse();

    // ── NUEVOS: filtrados por usuario — la DB hace el trabajo, no Java ────────
    List<ConfiguracionDePago> findByActivoTrueAndMetodoDePago_Usuario(Usuario usuario);
    List<ConfiguracionDePago> findByActivoFalseAndMetodoDePago_Usuario(Usuario usuario);

    Optional<ConfiguracionDePago>
    findByMetodoDePagoAndActivoTrueAndMetodoDePago_Usuario(
            MetodoDePago metodo,
            Usuario usuario
    );
}
