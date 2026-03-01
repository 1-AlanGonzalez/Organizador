package com.gymmanager.gym_manager.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;

import com.gymmanager.gym_manager.entity.EstadoPago;
import com.gymmanager.gym_manager.entity.MetodoDePago;
import com.gymmanager.gym_manager.entity.Pago;
import com.gymmanager.gym_manager.entity.Usuario;

public interface PagoRepository extends JpaRepository<Pago, Integer> {
    
    Optional<Pago> findByActividadCliente_IdActividadClienteAndEstado(Integer idActividadCliente, EstadoPago estado);
    List<Pago> findByActividadCliente_Cliente_IdClienteOrderByFechaGeneracionDesc(Integer idCliente);
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

    // Ingresos mensuales (para gráfico)
    // @Query(value = """
    // SELECT MONTH(fecha_generacion) AS mes,
    //        SUM(monto_a_pagar) AS total
    // FROM pago
    // WHERE estado_pago = 'PAGADO'
    //   AND YEAR(fecha_generacion) = YEAR(CURDATE())
    // GROUP BY MONTH(fecha_generacion)
    // ORDER BY MONTH(fecha_generacion)
    // """, nativeQuery = true)
    // List<Object[]> obtenerIngresosMensuales();
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



    // @Query("SELECT COALESCE(SUM(p.montoAPagar), 0) FROM Pago p " +
    //     "WHERE p.estado = 'PAGADO' " +
    //     "AND MONTH(p.fechaGeneracion) = :mes " +
    //     "AND YEAR(p.fechaGeneracion) = :anio")
    // BigDecimal sumTotalRecaudadoEnMes(@Param("mes") int mes, @Param("anio") int anio);
    @Query("SELECT COALESCE(SUM(p.montoAPagar), 0) FROM Pago p " +
           "WHERE p.estado = 'PAGADO' " +
           "AND MONTH(p.fechaGeneracion) = :mes " +
           "AND YEAR(p.fechaGeneracion)  = :anio " +
           "AND p.actividadCliente.cliente.usuario = :usuario")
    BigDecimal sumTotalRecaudadoEnMes(
            @Param("mes")     int mes,
            @Param("anio")    int anio,
            @Param("usuario") Usuario usuario);

    List<Pago> findByActividadCliente_Cliente_Usuario(Usuario usuario);


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

}  
