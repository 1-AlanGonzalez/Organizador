package com.gymmanager.gym_manager.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gymmanager.gym_manager.entity.Actividad;
import com.gymmanager.gym_manager.entity.Dicta;
import com.gymmanager.gym_manager.entity.Instructor;

public interface DictaRepository extends JpaRepository<Dicta,Integer> {
    boolean existsByInstructor(Instructor instructor);

    boolean existsByActividad(Actividad actividad);
}
