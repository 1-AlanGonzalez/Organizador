package com.gymmanager.gym_manager.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gymmanager.gym_manager.entity.Instructor;
import com.gymmanager.gym_manager.entity.Usuario;

public interface InstructorRepository extends JpaRepository<Instructor, Integer> {

    List<Instructor> findByUsuario(Usuario usuario);
    boolean existsByDniAndUsuario(String dni, Usuario usuario);
}
