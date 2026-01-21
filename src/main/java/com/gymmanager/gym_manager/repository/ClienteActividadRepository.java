package com.gymmanager.gym_manager.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gymmanager.gym_manager.entity.Actividad;
import com.gymmanager.gym_manager.entity.ActividadCliente;
import com.gymmanager.gym_manager.entity.Cliente;
import com.gymmanager.gym_manager.entity.EstadoInscripcion;

public interface ClienteActividadRepository extends JpaRepository<ActividadCliente, Integer> {
    
    Integer countByActividadAndEstado(Actividad actividad, EstadoInscripcion estado);

    boolean existsByClienteAndActividadAndEstado(Cliente cliente,Actividad actividad,EstadoInscripcion estado);

    Optional<ActividadCliente> findByClienteAndActividadAndEstado(Cliente cliente, Actividad actividad, EstadoInscripcion estado);

}
