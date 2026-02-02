package com.gymmanager.gym_manager.initializers;

import java.math.BigDecimal;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.gymmanager.gym_manager.entity.ConfiguracionDePago;
import com.gymmanager.gym_manager.entity.MetodoDePago;
import com.gymmanager.gym_manager.repository.ConfiguracionPagoRepository;


// el CommandLineRunner:
//Es una interfaz de Spring Boot.
//Sirve para ejecutar código automáticamente cuando la aplicación arranca
//Se ejecuta una sola vez por arranque

//Spring Boot hace esto:
// Crea el contexto de Spring
// Inyecta los repositories, services, controllers
// Busca clases que implementen CommandLineRunner
// Ejecuta el método run()
// Recién ahí levanta la web (localhost:8080)

//El component:
//Esta clase es parte del sistema, creala automáticamente

//Esta clase la usamos para crear configuraciones iniciales (como los recargos de pago) 
//solo si no existen, así el sistema siempre arranca con reglas válidas.”

@Component
public class ConfiguracionPagoInitializer implements  CommandLineRunner {
    private final ConfiguracionPagoRepository configuracionPagoRepository;

    public ConfiguracionPagoInitializer(ConfiguracionPagoRepository configuracionPagoRepository) {
    this.configuracionPagoRepository = configuracionPagoRepository;
    }

    //Este método se ejecuta SOLO al iniciar la aplicación
    @Override
    public void run(String... args) {
        crearSiNoExiste(MetodoDePago.EFECTIVO, BigDecimal.ZERO);
        crearSiNoExiste(MetodoDePago.TRANSFERENCIA, BigDecimal.ZERO);
        crearSiNoExiste(MetodoDePago.TARJETA, BigDecimal.valueOf(15));


    }

    private void crearSiNoExiste(MetodoDePago metodo, BigDecimal porcentaje) {

        boolean existe = configuracionPagoRepository.existsByMetodoDePagoAndActivoTrue(metodo);

        if (!existe) {
            ConfiguracionDePago config = new ConfiguracionDePago();
            config.setActivo(true);
            config.setMetodoDePago(metodo);
            config.setPorcentajeRecargo(porcentaje);
            

            configuracionPagoRepository.save(config);
        }
    }
}
