package com.gymmanager.gym_manager.services.exports;


import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.gymmanager.gym_manager.entity.Pago;
import com.gymmanager.gym_manager.entity.Usuario;
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
    public List<Map<String, Object>> exportar(EntidadRequestDTO request, LocalDate fecha, Usuario usuario) {
        List<Pago> pagos = (fecha != null)
            ? pagoRepository.findByActividadCliente_Cliente_UsuarioAndFechaGeneracion(usuario, fecha)
            : pagoRepository.findByActividadCliente_Cliente_Usuario(usuario);

        List<Map<String, Object>> filas = new ArrayList<>();
        for (Pago pago : pagos) {
            filas.addAll(ExportMapper.mapearEntidad(pago, request.getAtributos()));
        }
        return filas;
    }

    @Override
    public List<Map<String, Object>> exportar(EntidadRequestDTO request, LocalDate fecha,
                                               String mes, Usuario usuario) {
        List<Pago> pagos;
        if (mes != null && !mes.isBlank()) {
            YearMonth periodo;
            try {
                periodo = YearMonth.parse(mes);
            } catch (java.time.format.DateTimeParseException e) {
                throw new IllegalArgumentException("El mes seleccionado no es válido.", e);
            }
            pagos = pagoRepository
                    .findByActividadCliente_Cliente_UsuarioAndFechaGeneracionBetween(
                            usuario, periodo.atDay(1), periodo.atEndOfMonth());
        } else {
            pagos = (fecha != null)
                    ? pagoRepository.findByActividadCliente_Cliente_UsuarioAndFechaGeneracion(usuario, fecha)
                    : pagoRepository.findByActividadCliente_Cliente_Usuario(usuario);
        }

        List<Map<String, Object>> filas = new ArrayList<>();
        for (Pago pago : pagos) {
            filas.addAll(ExportMapper.mapearEntidad(pago, request.getAtributos()));
        }
        return filas;
    }
}
