package com.gymmanager.gym_manager.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gymmanager.gym_manager.entity.ActividadCliente;
import com.gymmanager.gym_manager.entity.Asistencia;
import com.gymmanager.gym_manager.entity.Usuario;

public interface AsistenciaRepository extends JpaRepository<Asistencia, Integer> {
        boolean existsByFechaAndActividadCliente(LocalDate fecha, ActividadCliente actividadCliente);
        Asistencia findByFechaAndActividadCliente(LocalDate fecha, ActividadCliente actividadCliente);
        List<Asistencia> findByActividadCliente_Cliente_Usuario(Usuario usuario);
        List<Asistencia> findByFecha(LocalDate fecha);
        List<Asistencia> findByActividadCliente_Cliente_UsuarioAndFecha(Usuario usuario, LocalDate fecha);

}
