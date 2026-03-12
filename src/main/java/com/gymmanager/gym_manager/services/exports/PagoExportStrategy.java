package com.gymmanager.gym_manager.services.exports;

import java.lang.reflect.Field;
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
        return "pagos";
    }

    @Override
    public List<Map<String, Object>> exportar(EntidadRequestDTO request, LocalDate fecha) {
        System.out.println("Atributos pagos: " + request.getAtributos());
        for (Field f : Pago.class.getDeclaredFields()) {
        System.out.println("Campo real: " + f.getName());
    }
        List<Pago> pagos = pagoRepository.findAll();
        List<Map<String, Object>> filas = new ArrayList<>();

        for (Pago pago : pagos) {
            System.out.println("Procesando pago: " + pago.getIdPago());
            List<Map<String, Object>> filasPago =
                    ExportMapper.mapearEntidad(pago, request.getAtributos());
            System.out.println("Filas generadas: " + filasPago);
            filas.addAll(filasPago);
        }

        return filas;
    }
}
