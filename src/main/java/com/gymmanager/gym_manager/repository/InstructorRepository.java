package com.gymmanager.gym_manager.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gymmanager.gym_manager.entity.Instructor;

public interface InstructorRepository extends JpaRepository<Instructor, Integer> {
    boolean existsByDni(String dni);
}
