package com.gymmanager.gym_manager.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gymmanager.gym_manager.entity.Actividad;
import com.gymmanager.gym_manager.entity.Usuario;

public interface ActividadRepository extends JpaRepository<Actividad, Integer> {

    List<Actividad> findByUsuario(Usuario usuario);

    Optional<Actividad> findByIdActividadAndUsuario(Integer id, Usuario usuario);
}
