package com.gymmanager.gym_manager.repository;

import java.math.BigDecimal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gymmanager.gym_manager.entity.Pago;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    @Query("SELECT SUM(p.montoAPagar) FROM Pago p WHERE p.estado = 'ADEUDA'")
    BigDecimal sumTotalPendiente();
    
    @Query("SELECT SUM(p.montoAPagar) FROM Pago p WHERE p.estado = 'PAGADO'")
    BigDecimal sumTotalRecaudado();

    @Query("SELECT SUM(p.montoAPagar) FROM Pago p WHERE p.estado = 'PAGADO' AND p.metodoPago = 'EFECTIVO'")
    BigDecimal sumEfectivo();

    @Query("SELECT SUM(p.montoAPagar) FROM Pago p WHERE p.estado = 'PAGADO' AND p.metodoPago = 'TRANSFERENCIA'")
    BigDecimal sumTransferencia();

}
