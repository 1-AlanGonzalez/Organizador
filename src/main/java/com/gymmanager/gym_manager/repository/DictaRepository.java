package com.gymmanager.gym_manager.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gymmanager.gym_manager.entity.Actividad;
import com.gymmanager.gym_manager.entity.Dicta;
import com.gymmanager.gym_manager.entity.Instructor;

public interface DictaRepository extends JpaRepository<Dicta,Integer> {
    boolean existsByInstructor(Instructor instructor);
    // Optional sirve para manejar valores que pueden ser nulos
    Optional<Dicta> findByActividadAndInstructor(Actividad actividad, Instructor instructor);

    boolean existsByActividad(Actividad actividad);
}
