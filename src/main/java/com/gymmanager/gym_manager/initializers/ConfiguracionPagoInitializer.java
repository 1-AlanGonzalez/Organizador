package com.gymmanager.gym_manager.initializers;

import java.math.BigDecimal;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.gymmanager.gym_manager.entity.ConfiguracionDePago;
import com.gymmanager.gym_manager.entity.MetodoDePago;
import com.gymmanager.gym_manager.repository.ConfiguracionPagoRepository;
import com.gymmanager.gym_manager.repository.MetodoDePagoRepository;


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
    private final MetodoDePagoRepository metodoDePagoRepository;
    private final ConfiguracionPagoRepository configuracionPagoRepository;

    public ConfiguracionPagoInitializer(MetodoDePagoRepository metodoDePagoRepository, ConfiguracionPagoRepository configuracionPagoRepository) {
    this.metodoDePagoRepository = metodoDePagoRepository;
    this.configuracionPagoRepository = configuracionPagoRepository;
    }

    //Este método se ejecuta SOLO al iniciar la aplicación
    @Override
    public void run(String... args) {
        crearSiNoExiste("NO_ESPECIFICADO", BigDecimal.ZERO);
        crearSiNoExiste("EFECTIVO", BigDecimal.ZERO);
        crearSiNoExiste("TRANSFERENCIA", BigDecimal.ZERO);
        crearSiNoExiste("TARJETA/CREDITO", BigDecimal.valueOf(15));


    }

    private void crearSiNoExiste(String nombre, BigDecimal porcentaje) {

    MetodoDePago metodo = metodoDePagoRepository
            .findByNombre(nombre)
            .orElseGet(() -> {
                MetodoDePago nuevo = new MetodoDePago(nombre);
                return metodoDePagoRepository.save(nuevo);
            });

    boolean existeConfig = configuracionPagoRepository.existsByMetodoDePago(metodo);
    System.out.println(existeConfig);
    System.out.println(metodo.getNombre());
    if (!existeConfig) {
        ConfiguracionDePago config = new ConfiguracionDePago();
        config.setMetodoDePago(metodo);
        config.cambiarOAgregarRecargo(porcentaje);
        config.activarMetodo();

        System.out.println(config.getMetodoDePago().getNombre());
        System.out.println(config.getActivo());
        System.out.println(config.getPorcentajeRecargo());
        configuracionPagoRepository.save(config);
    }
}
}

