package com.gymmanager.gym_manager.repository;

import java.math.BigDecimal;
import java.util.List;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gymmanager.gym_manager.entity.Pago;

public interface PagoRepository extends JpaRepository<Pago, Integer> {

    @Query("SELECT SUM(p.montoAPagar) FROM Pago p WHERE p.estado = 'ADEUDA'")
    BigDecimal sumTotalPendiente();
    
    @Query("SELECT SUM(p.montoAPagar) FROM Pago p WHERE p.estado = 'PAGADO'")
    BigDecimal sumTotalRecaudado();

    @Query("SELECT SUM(p.montoAPagar) FROM Pago p WHERE p.estado = 'PAGADO' AND p.metodoPago = 'EFECTIVO'")
    BigDecimal sumEfectivo();

    @Query("SELECT SUM(p.montoAPagar) FROM Pago p WHERE p.estado = 'PAGADO' AND p.metodoPago = 'TRANSFERENCIA'")
    BigDecimal sumTransferencia();

// Esto sirve para el reporte de ingresos mensuales
    // Devuelve una lista con los totales mensuales
    // Para mostrar en el gráfico del frontend
    @Query(value = "SELECT SUM(monto_a_pagar) FROM pago " +
                "WHERE estado_pago = 'PAGADO' " +
                "GROUP BY MONTH(fecha_generacion) " +
                "ORDER BY MONTH(fecha_generacion) ASC", nativeQuery = true)
    List<BigDecimal> obtenerIngresosMensuales();    
}   
