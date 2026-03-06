package com.gymmanager.gym_manager.services.exports;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.gymmanager.gym_manager.entity.Pago;
import com.gymmanager.gym_manager.entity.dto.EntidadRequestDTO;
import com.gymmanager.gym_manager.repository.PagoRepository;

@Component
public class PagoExportStrategy implements ExportStrategy {

    private final PagoRepository pagoRepository;

    public PagoExportStrategy(PagoRepository pagoRepository) {
        this.pagoRepository = pagoRepository;
    }

    @Override
    public String getNombreEntidad() {
        return "pago";
    }

    @Override
    public List<Map<String, Object>> exportar(EntidadRequestDTO request, LocalDate fecha) {

        List<Pago> pagos = pagoRepository.findAll();
        List<Map<String, Object>> filas = new ArrayList<>();

        for (Pago pago : pagos) {

            List<Map<String, Object>> filasPago =
                    ExportMapper.mapearEntidad(pago, request.getAtributos());

            filas.addAll(filasPago);
        }

        return filas;
    }
}
