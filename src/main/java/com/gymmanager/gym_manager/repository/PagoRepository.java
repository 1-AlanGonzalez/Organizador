package com.gymmanager.gym_manager.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;

import com.gymmanager.gym_manager.entity.ActividadCliente;
import com.gymmanager.gym_manager.entity.EstadoPago;
import com.gymmanager.gym_manager.entity.MetodoDePago;
import com.gymmanager.gym_manager.entity.Pago;
import com.gymmanager.gym_manager.entity.Usuario;

public interface PagoRepository extends JpaRepository<Pago, Integer> {
    
    Optional<Pago> findByActividadCliente_IdActividadClienteAndEstado(Integer idActividadCliente, EstadoPago estado);
    List<Pago> findByActividadCliente_Cliente_IdClienteOrderByFechaGeneracionDesc(Integer idCliente);
    List<Pago>
            findByActividadCliente_Cliente_IdClienteAndActividadCliente_Cliente_UsuarioOrderByFechaGeneracionDesc(
            Integer idCliente,
            Usuario usuario
    );
    void deleteByMetodoPago(MetodoDePago metodo);
    

    // @Query("SELECT SUM(p.montoAPagar) FROM Pago p WHERE p.estado = 'ADEUDA'")
    // BigDecimal sumTotalPendiente();
    @Query("SELECT COALESCE(SUM(p.montoAPagar), 0) FROM Pago p " +
           "WHERE p.estado = 'ADEUDA' " +
           "AND p.actividadCliente.cliente.usuario = :usuario")
    BigDecimal sumTotalPendiente(@Param("usuario") Usuario usuario);

    // Total recaudado (PAGADO)
    // @Query("SELECT SUM(p.montoAPagar) FROM Pago p WHERE p.estado = 'PAGADO'")
    // BigDecimal sumTotalRecaudado();
    @Query("SELECT COALESCE(SUM(p.montoAPagar), 0) FROM Pago p " +
           "WHERE p.estado = 'PAGADO' " +
           "AND p.actividadCliente.cliente.usuario = :usuario")
    BigDecimal sumTotalRecaudado(@Param("usuario") Usuario usuario);

    // Total recaudado por método de pago
    // @Query("""
    //     SELECT SUM(p.montoAPagar)
    //     FROM Pago p
    //     WHERE p.estado = 'PAGADO'
    //     AND p.metodoPago = :metodo
    // """)
    // BigDecimal sumPorMetodo(@Param("metodo") MetodoDePago metodo);
    @Query("""
        SELECT COALESCE(SUM(p.montoAPagar), 0) FROM Pago p
        WHERE p.estado = 'PAGADO'
        AND p.metodoPago = :metodo
        AND p.actividadCliente.cliente.usuario = :usuario
        """)
    BigDecimal sumPorMetodo(
            @Param("metodo")  MetodoDePago metodo,
            @Param("usuario") Usuario usuario);


    @Query(value = """
        SELECT MONTH(fecha_generacion) AS mes,
               SUM(monto_a_pagar) AS total
        FROM pago
        INNER JOIN actividad_cliente ac ON pago.id_actividad_cliente = ac.id_actividad_cliente
        INNER JOIN cliente c ON ac.id_cliente = c.id_cliente
        WHERE pago.estado_pago = 'PAGADO'
          AND YEAR(fecha_generacion) = YEAR(CURDATE())
          AND c.usuario_id = :usuarioId
        GROUP BY MONTH(fecha_generacion)
        ORDER BY MONTH(fecha_generacion)
        """, nativeQuery = true)
    List<Object[]> obtenerIngresosMensuales(@Param("usuarioId") Integer usuarioId);


    @Query("SELECT COALESCE(SUM(p.montoAPagar), 0) FROM Pago p " +
           "WHERE p.estado = 'PAGADO' " +
           "AND MONTH(p.fechaGeneracion) = :mes " +
           "AND YEAR(p.fechaGeneracion)  = :anio " +
           "AND p.actividadCliente.cliente.usuario = :usuario")
    BigDecimal sumTotalRecaudadoEnMes(
            @Param("mes")     int mes,
            @Param("anio")    int anio,
            @Param("usuario") Usuario usuario);

    List<Pago> findByActividadCliente_Cliente_UsuarioAndFechaVencimientoLessThanEqual(
        Usuario usuario,
        LocalDate fecha
);


// ── Agregar estos 3 métodos a PagoRepository.java ────────────────────────────
// (los existentes sumTotalRecaudado, sumTotalPendiente, sumPorMetodo se pueden
//  mantener si los usás en otro lado, o reemplazar si solo los usabas en ingresos)


// Total recaudado (PAGADO) en un rango de fechas
@Query("SELECT COALESCE(SUM(p.montoAPagar), 0) FROM Pago p " +
       "WHERE p.actividadCliente.cliente.usuario = :usuario " +
       "AND p.estado = 'PAGADO' " +
       "AND p.fechaGeneracion BETWEEN :desde AND :hasta")
BigDecimal sumTotalRecaudadoEntreFechas(@Param("usuario") Usuario usuario,
                                        @Param("desde")   LocalDate desde,
                                        @Param("hasta")   LocalDate hasta);

// Total adeudado en un rango de fechas
@Query("SELECT COALESCE(SUM(p.montoAPagar), 0) FROM Pago p " +
       "WHERE p.actividadCliente.cliente.usuario = :usuario " +
       "AND p.estado = 'ADEUDA' " +
       "AND p.fechaGeneracion BETWEEN :desde AND :hasta")
BigDecimal sumTotalPendienteEntreFechas(@Param("usuario") Usuario usuario,
                                        @Param("desde")   LocalDate desde,
                                        @Param("hasta")   LocalDate hasta);

// Total por método de pago en un rango de fechas
@Query("SELECT COALESCE(SUM(p.montoAPagar), 0) FROM Pago p " +
       "WHERE p.metodoPago = :metodo " +
       "AND p.actividadCliente.cliente.usuario = :usuario " +
       "AND p.estado = 'PAGADO' " +
       "AND p.fechaGeneracion BETWEEN :desde AND :hasta")
BigDecimal sumPorMetodoEntreFechas(@Param("metodo")  MetodoDePago metodo,
                                   @Param("usuario") Usuario usuario,
                                   @Param("desde")   LocalDate desde,
                                   @Param("hasta")   LocalDate hasta);


Optional<Pago> findTopByActividadClienteOrderByFechaVencimientoDesc(ActividadCliente actividadCliente);
Optional<Pago> findTopByActividadCliente_IdActividadClienteOrderByFechaVencimientoDesc(Integer idActividadCliente);
Optional<Pago> findByActividadCliente_IdActividadClienteAndFechaGeneracion(
        Integer idActividadCliente,
        LocalDate fechaGeneracion
);
@Query("SELECT p FROM Pago p " +
       "WHERE p.actividadCliente.cliente.usuario = :usuario " +
       "AND (p.estado = 'PAGADO' OR p.estado = 'ADEUDA') " +
       "AND p.actividadCliente.estado = 'ACTIVA' " +
       "ORDER BY p.fechaGeneracion DESC")
    List<Pago> findPagosVisibles(@Param("usuario") Usuario usuario);

    @Query("SELECT p FROM Pago p " +
           "WHERE p.actividadCliente.cliente.usuario = :usuario " +
           "AND (p.estado = 'PAGADO' OR p.estado = 'ADEUDA') " +
           "AND p.actividadCliente.estado = 'ACTIVA' " +
           "ORDER BY p.fechaGeneracion DESC")
    Page<Pago> findPagosVisibles(@Param("usuario") Usuario usuario, Pageable pageable);

    @Query("""
        SELECT p FROM Pago p
        WHERE p.actividadCliente.cliente.usuario = :usuario
          AND (p.estado = 'PAGADO' OR p.estado = 'ADEUDA')
          AND p.actividadCliente.estado = 'ACTIVA'
          AND (:texto = ''
               OR LOWER(p.actividadCliente.cliente.nombre) LIKE LOWER(CONCAT('%', :texto, '%'))
               OR LOWER(p.actividadCliente.cliente.apellido) LIKE LOWER(CONCAT('%', :texto, '%'))
               OR LOWER(p.actividadCliente.actividad.nombre) LIKE LOWER(CONCAT('%', :texto, '%')))
          AND (:estado IS NULL OR p.estado = :estado)
          AND (:desde IS NULL OR p.fechaGeneracion >= :desde)
          AND (:hasta IS NULL OR p.fechaGeneracion <= :hasta)
        ORDER BY p.fechaGeneracion DESC
        """)
    Page<Pago> buscarVisibles(@Param("usuario") Usuario usuario,
                              @Param("texto") String texto,
                              @Param("estado") EstadoPago estado,
                              @Param("desde") LocalDate desde,
                              @Param("hasta") LocalDate hasta,
                              Pageable pageable);

    Optional<Pago> findTopByActividadCliente_IdActividadClienteOrderByFechaGeneracionDesc(Integer idActividadCliente);
    List<Pago> findByFechaGeneracion(LocalDate fechaGeneracion);
    List<Pago> findByActividadCliente_Cliente_UsuarioAndFechaGeneracion(Usuario usuario, LocalDate fechaGeneracion);
    List<Pago> findByActividadCliente_Cliente_UsuarioAndFechaGeneracionBetween(
            Usuario usuario, LocalDate desde, LocalDate hasta);
    List<Pago> findByActividadCliente_Cliente_Usuario(Usuario usuario);

    Optional<Pago>
            findByActividadCliente_IdActividadClienteAndEstadoAndActividadCliente_Cliente_Usuario(
            Integer idActividadCliente,
            EstadoPago estado,
            Usuario usuario
    );
    Optional<Pago> findByIdPagoAndActividadCliente_Cliente_Usuario(
            Integer idPago,
            Usuario usuario
    );

}  
