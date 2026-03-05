package com.gymmanager.gym_manager.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gymmanager.gym_manager.entity.Cliente;
import com.gymmanager.gym_manager.entity.EstadoInscripcion;
import com.gymmanager.gym_manager.entity.Usuario;



public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
    // boolean existsByDni(String dni);

    // Añadido para el multi-tenant 
    List<Cliente> findByUsuario(Usuario usuario);
    boolean existsByDniAndUsuario(String dni, Usuario usuario);
    Optional<Cliente> findByIdClienteAndUsuario(Integer id, Usuario usuario);


    // @Query("""
    // SELECT DISTINCT c
    // FROM Cliente c
    // LEFT JOIN FETCH c.inscripciones i
    // LEFT JOIN FETCH i.actividad
    // """)
    // List<Cliente> findAllConInscripciones();
 // ── Consulta completa con JOIN FETCH, filtrada por usuario ────────────────

    @Query("""
        SELECT DISTINCT c
        FROM Cliente c
        LEFT JOIN FETCH c.inscripciones i
        LEFT JOIN FETCH i.actividad
        WHERE c.usuario = :usuario
        """)
    List<Cliente> findAllConInscripciones(@Param("usuario") Usuario usuario);


    // @Query("SELECT COUNT(DISTINCT c) FROM Cliente c JOIN c.inscripciones i WHERE i.estado = :estado")
    // long countClientesConInscripcionActiva(@Param("estado") EstadoInscripcion estado);
    @Query("""
        SELECT COUNT(DISTINCT c) FROM Cliente c
        JOIN c.inscripciones i
        WHERE i.estado = :estado
        AND c.usuario = :usuario
        """)
    long countClientesConInscripcionActiva(
            @Param("estado") EstadoInscripcion estado,
            @Param("usuario") Usuario usuario);

    // @Query("SELECT COUNT(DISTINCT c) FROM Cliente c JOIN c.inscripciones i JOIN i.pagos p " +
    //        "WHERE i.estado = 'ACTIVA' AND (p.estado = 'ADEUDA' OR p.estado = 'VENCIDO')")
    // long countClientesDeudores();
    @Query("""
        SELECT COUNT(DISTINCT c) FROM Cliente c
        JOIN c.inscripciones i
        JOIN i.pagos p
        WHERE i.estado = 'ACTIVA'
        AND (p.estado = 'ADEUDA' OR p.estado = 'VENCIDO')
        AND c.usuario = :usuario
        """)
    long countClientesDeudores(@Param("usuario") Usuario usuario);



    
}