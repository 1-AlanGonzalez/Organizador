package com.gymmanager.gym_manager.services;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.gymmanager.gym_manager.entity.ConfiguracionDePago;
import com.gymmanager.gym_manager.repository.ConfiguracionPagoRepository;

import jakarta.transaction.Transactional;

@Service
public class ConfiguracionService {
    private final ConfiguracionPagoRepository configuracionPagoRepository;

    
    public ConfiguracionService(ConfiguracionPagoRepository configuracionPagoRepository) {
        this.configuracionPagoRepository = configuracionPagoRepository;
    }


    @Transactional
    public void cambiarPorcentajeInteres(Integer idConfiguracion, BigDecimal porcentaje){
        ConfiguracionDePago config = configuracionPagoRepository
            .findById(idConfiguracion)
            .orElseThrow(() ->
                new RuntimeException("La configuración no existe en el sistema")
            );
        config.cambiarOAgregarRecargo(porcentaje);

        configuracionPagoRepository.save(config);

    }
}
