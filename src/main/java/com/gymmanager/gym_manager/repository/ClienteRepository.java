package com.gymmanager.gym_manager.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.gymmanager.gym_manager.entity.Cliente;
import com.gymmanager.gym_manager.entity.EstadoInscripcion;
import com.gymmanager.gym_manager.entity.Usuario;



public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
    // boolean existsByDni(String dni);

    // Añadido para el multi-tenant 
    List<Cliente> findByUsuario(Usuario usuario);
    Page<Cliente> findByUsuario(Usuario usuario, Pageable pageable);
    @Query(value = """
        SELECT DISTINCT c
        FROM Cliente c
        LEFT JOIN c.inscripciones i
        LEFT JOIN i.actividad a
        WHERE c.usuario = :usuario
          AND (:texto = ''
               OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', :texto, '%'))
               OR LOWER(c.apellido) LIKE LOWER(CONCAT('%', :texto, '%'))
               OR LOWER(COALESCE(c.dni, '')) LIKE LOWER(CONCAT('%', :texto, '%')))
          AND (:actividad = '' OR a.nombre = :actividad)
        """,
        countQuery = """
        SELECT COUNT(DISTINCT c.idCliente)
        FROM Cliente c
        LEFT JOIN c.inscripciones i
        LEFT JOIN i.actividad a
        WHERE c.usuario = :usuario
          AND (:texto = ''
               OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', :texto, '%'))
               OR LOWER(c.apellido) LIKE LOWER(CONCAT('%', :texto, '%'))
               OR LOWER(COALESCE(c.dni, '')) LIKE LOWER(CONCAT('%', :texto, '%')))
          AND (:actividad = '' OR a.nombre = :actividad)
        """)
    Page<Cliente> buscarPagina(
            @Param("usuario") Usuario usuario,
            @Param("texto") String texto,
            @Param("actividad") String actividad,
            Pageable pageable);
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


    // NUEVO 
    // ─── Agregar estos dos métodos en ClienteRepository.java ───────────────────
// La cadena es: Cliente → inscripciones (ActividadCliente) → pagos (Pago)

/**
 * Clientes que tienen al menos un pago vencido (estaVencido = estado ADEUDA y fecha pasada).
 * Doble JOIN: inscripciones → pagos
 */
@Query("SELECT DISTINCT c FROM Cliente c " +
       "JOIN c.inscripciones i " +
       "JOIN i.pagos p " +
       "WHERE c.usuario = :usuario " +
       "AND p.estado = com.gymmanager.gym_manager.entity.EstadoPago.ADEUDA ")
List<Cliente> findClientesConDeudaByUsuario(@Param("usuario") Usuario usuario);

/**
 * Clientes cuyo pago activo (ADEUDA) vence entre [desde] y [hasta].
 * Útil para mostrar quiénes están por vencer en los próximos N días.
 */
@Query("SELECT DISTINCT c FROM Cliente c " +
       "JOIN c.inscripciones i " +
       "JOIN i.pagos p " +
       "WHERE c.usuario = :usuario " +
       "AND i.estado = com.gymmanager.gym_manager.entity.EstadoInscripcion.ACTIVA " +
       "AND p.estado = com.gymmanager.gym_manager.entity.EstadoPago.ADEUDA " +
       "AND p.fechaVencimiento BETWEEN :desde AND :hasta")
List<Cliente> findClientesConVencimientoEntre(
        @Param("usuario") Usuario usuario,
        @Param("desde")   LocalDate desde,
        @Param("hasta")   LocalDate hasta);
    
}
