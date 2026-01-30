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


    // AGREGADO HOY 29/1
    // 1. Cuenta clientes que tienen AL MENOS una inscripción ACTIVA
    @Query("SELECT COUNT(DISTINCT c) FROM Cliente c JOIN c.inscripciones i WHERE i.estado = 'ACTIVA'")
    long countClientesConInscripcionActiva();

    // 2. Cuenta clientes que tienen inscripción ACTIVA y pagos PENDIENTES (Adeuda o Vencido)
    @Query("SELECT COUNT(DISTINCT c) FROM Cliente c JOIN c.inscripciones i JOIN i.pagos p " +
           "WHERE i.estado = 'ACTIVA' AND (p.estado = 'ADEUDA' OR p.estado = 'VENCIDO')")
    long countClientesDeudores();
}