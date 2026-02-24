package com.gymmanager.gym_manager.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gymmanager.gym_manager.entity.EstadoPago;
import com.gymmanager.gym_manager.entity.MetodoDePago;
import com.gymmanager.gym_manager.entity.Pago;

public interface PagoRepository extends JpaRepository<Pago, Integer> {

//     @Query("SELECT SUM(p.montoAPagar) FROM Pago p WHERE p.estado = 'ADEUDA'")
//     BigDecimal sumTotalPendiente();
    
//     @Query("SELECT SUM(p.montoAPagar) FROM Pago p WHERE p.estado = 'PAGADO'")
//     BigDecimal sumTotalRecaudado();

//     @Query("SELECT SUM(p.montoAPagar) FROM Pago p WHERE p.estado = 'PAGADO' AND p.metodoPago = 'EFECTIVO'")
//     BigDecimal sumEfectivo();

//     @Query("SELECT SUM(p.montoAPagar) FROM Pago p WHERE p.estado = 'PAGADO' AND p.metodoPago = 'TRANSFERENCIA'")
//     BigDecimal sumTransferencia();

// // Esto sirve para el reporte de ingresos mensuales
//     // Devuelve una lista con los totales mensuales
//     // Para mostrar en el gráfico del frontend
//     @Query(value = "SELECT SUM(monto_a_pagar) FROM pago " +
//                 "WHERE estado_pago = 'PAGADO' " +
//                 "GROUP BY MONTH(fecha_generacion) " +
//                 "ORDER BY MONTH(fecha_generacion) ASC", nativeQuery = true)
//     List<BigDecimal> obtenerIngresosMensuales();    
// 
    // Total pendiente (ADEUDA)
    @Query("SELECT SUM(p.montoAPagar) FROM Pago p WHERE p.estado = 'ADEUDA'")
    BigDecimal sumTotalPendiente();

    // Total recaudado (PAGADO)
    @Query("SELECT SUM(p.montoAPagar) FROM Pago p WHERE p.estado = 'PAGADO'")
    BigDecimal sumTotalRecaudado();

    // Total recaudado por método de pago
    @Query("""
        SELECT SUM(p.montoAPagar)
        FROM Pago p
        WHERE p.estado = 'PAGADO'
        AND p.metodoPago = :metodo
    """)
    BigDecimal sumPorMetodo(@Param("metodo") MetodoDePago metodo);

    // Ingresos mensuales (para gráfico)
    @Query(value = """
    SELECT MONTH(fecha_generacion) AS mes,
           SUM(monto_a_pagar) AS total
    FROM pago
    WHERE estado_pago = 'PAGADO'
      AND YEAR(fecha_generacion) = YEAR(CURDATE())
    GROUP BY MONTH(fecha_generacion)
    ORDER BY MONTH(fecha_generacion)
    """, nativeQuery = true)
    List<Object[]> obtenerIngresosMensuales();
    
    
    // Busca un pago asociado a una inscripción (ActividadCliente)
    // filtrando además por el estado del pago.
 
    // Se usa principalmente para:
    // Obtener el pago pendiente (ADEUDA) de una inscripción
    // Evitar crear pagos duplicados para la misma actividad
    
    Optional<Pago> findByActividadCliente_IdActividadClienteAndEstado(
        Integer idActividadCliente,
        EstadoPago estado);

    // Para el ver_clientes añado este método
    List<Pago> findByActividadCliente_Cliente_IdClienteOrderByFechaGeneracionDesc(Integer idCliente);


    @Query("SELECT COALESCE(SUM(p.montoAPagar), 0) FROM Pago p " +
        "WHERE p.estado = 'PAGADO' " +
        "AND MONTH(p.fechaGeneracion) = :mes " +
        "AND YEAR(p.fechaGeneracion) = :anio")
    BigDecimal sumTotalRecaudadoEnMes(@Param("mes") int mes, @Param("anio") int anio);

}  
