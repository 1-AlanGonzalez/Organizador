package com.gymmanager.gym_manager.services;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.gymmanager.gym_manager.config.SecurityUtils;
import com.gymmanager.gym_manager.entity.ConfiguracionDePago;
import com.gymmanager.gym_manager.repository.ConfiguracionPagoRepository;

import jakarta.transaction.Transactional;

@Service
public class ConfiguracionService {
    private final ConfiguracionPagoRepository configuracionPagoRepository;
    private final SecurityUtils securityUtils;

    
    public ConfiguracionService(ConfiguracionPagoRepository configuracionPagoRepository,
                                SecurityUtils securityUtils) {
        this.configuracionPagoRepository = configuracionPagoRepository;
        this.securityUtils = securityUtils;
    }


    @Transactional
    public void cambiarPorcentajeInteres(Integer idConfiguracion, BigDecimal porcentaje){
        ConfiguracionDePago config = configuracionPagoRepository
            .findByIdAndMetodoDePago_Usuario(idConfiguracion, securityUtils.getUsuarioActual())
            .orElseThrow(() ->
                new RuntimeException("La configuración no existe en el sistema")
            );
        config.cambiarOAgregarRecargo(porcentaje);

        configuracionPagoRepository.save(config);

    }
}
