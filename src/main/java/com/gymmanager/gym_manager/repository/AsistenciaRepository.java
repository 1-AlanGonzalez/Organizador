package com.gymmanager.gym_manager.repository;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gymmanager.gym_manager.entity.ActividadCliente;
import com.gymmanager.gym_manager.entity.Asistencia;

public interface AsistenciaRepository extends JpaRepository<Asistencia, Integer> {
        boolean existsByFechaAndActividadCliente(LocalDate fecha, ActividadCliente actividadCliente);

}
