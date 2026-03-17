package com.gymmanager.gym_manager.services;


import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.gymmanager.gym_manager.config.SecurityUtils;
import com.gymmanager.gym_manager.entity.Usuario;
import com.gymmanager.gym_manager.entity.dto.EntidadRequestDTO;
import com.gymmanager.gym_manager.entity.dto.ExportRequest;
import com.gymmanager.gym_manager.services.exports.ExportStrategy;


// va a encargarse de:

// Decidir si es multihoja o combinado

// Llamar a los services correctos

// Transformar entidades en datos exportables
@Service
public class ExportService {
    // Spring automáticamente detecta:
    // ClienteExportStrategy
    // (Mañana) PagoExportStrategy
    // (Mañana) AsistenciaExportStrategy
    // Y te las inyecta todas en una lista.
    // Eso es magia de Spring + @Component.

    private final Map<String, ExportStrategy> strategies = new LinkedHashMap<>();
    private final SecurityUtils securityUtils;

    public ExportService(List<ExportStrategy> strategyList, SecurityUtils securityUtils) {

        System.out.println("Strategies detectadas: " + strategyList.size());
        
        this.securityUtils = securityUtils;
        for (ExportStrategy strategy : strategyList) {
            System.out.println("Registrando: " + strategy.getNombreEntidad());
            strategies.put(strategy.getNombreEntidad().toLowerCase(), strategy);
        }
    }
    

    public Map<String, List<Map<String, Object>>> generarExportacion(ExportRequest request) {
    Usuario usuario = securityUtils.getUsuarioActual();
    System.out.println("Entidades pedidas: " + request.getEntidades().size());
    for (EntidadRequestDTO e : request.getEntidades()) {
    System.out.println("Entidad: " + e.getNombre());
    }
    Map<String, List<Map<String, Object>>> resultado = new LinkedHashMap<>();

    for (EntidadRequestDTO entidadDTO : request.getEntidades()) {

        String nombreEntidad = entidadDTO.getNombre().toLowerCase();

        ExportStrategy strategy = strategies.get(nombreEntidad);

        if (strategy == null) {
            throw new RuntimeException("No existe strategy para " + nombreEntidad);
        }

        List<Map<String, Object>> datos =
                strategy.exportar(entidadDTO, request.getFecha(), usuario);

        resultado.put(entidadDTO.getNombre(), datos);
    }

    return resultado;
}


  
}
