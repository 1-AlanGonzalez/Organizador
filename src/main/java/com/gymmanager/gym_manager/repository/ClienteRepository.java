package com.gymmanager.gym_manager.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gymmanager.gym_manager.entity.Cliente;



public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
    boolean existsByDni(String dni);
    // Query : traer todos los clientes con sus inscripciones y actividades asociadas
    // Select distinct para evitar duplicados
    // FETCH para cargar las inscripciones y actividades en la misma consulta
    @Query("""
    SELECT DISTINCT c
    FROM Cliente c
    LEFT JOIN FETCH c.inscripciones i
    LEFT JOIN FETCH i.actividad
    """)
    List<Cliente> findAllConInscripciones();
}