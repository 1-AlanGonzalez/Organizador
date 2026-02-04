package com.gymmanager.gym_manager.services;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.gymmanager.gym_manager.entity.Actividad;
import com.gymmanager.gym_manager.entity.Cliente;
import com.gymmanager.gym_manager.entity.EstadoInscripcion;
import com.gymmanager.gym_manager.entity.TipoDeCobro;
import com.gymmanager.gym_manager.repository.ActividadRepository;
import com.gymmanager.gym_manager.repository.ClienteActividadRepository;
import com.gymmanager.gym_manager.repository.ClienteRepository;

import jakarta.transaction.Transactional;

// TEST PARA LA CREACIÓN Y EDICIÓN DE CLIENTES 

@SpringBootTest
@Transactional
public class ClienteServiceTest {

    @Autowired
    ClienteService clienteService;

    @Autowired
    ClienteRepository clienteRepository;

    @Autowired
    ActividadRepository actividadRepository;

    @Autowired
    ClienteActividadRepository clienteActividadRepository;

    Actividad gym;
    Actividad boxeo;


    // Para cada test, primero inicializo las siguientes actividades
    @BeforeEach
    void setup() {
        gym = actividadRepository.save(
            new Actividad("Gym", 10, new BigDecimal("20000"), new BigDecimal("2500"))
        );

        boxeo = actividadRepository.save(
            new Actividad("Boxeo", 10, new BigDecimal("25000"), new BigDecimal("3000"))
        );
    }
    // Creo un nuevo cliente donde voy a utilizar la función guardarOActualizarCliente que es utilizada en el Controller,
    // guardando el cliente en sí, con una lista donde tengo la actividad GYM, la fecha de hoy y el tipo de cobro mensual.
    // Luego busco al cliente con el repository para verificar que se haya guardado correctamente, y lo setteo en la variable "guardado"
    // para posteriormente hacer un assertEquals y la cantidad de inscripciones (1)
    // 
    @Test
    void crearClienteConActividad_ok() {
        Cliente cliente = new Cliente("Juan", "Perez", "123", "111");

        clienteService.guardarOActualizarCliente(
            cliente,
            List.of(gym.getIdActividad()),
            LocalDate.now(),
            TipoDeCobro.MENSUAL
        );

        Cliente guardado = clienteRepository.findAll().get(0);

        assertEquals(1, guardado.getInscripciones().size());
    }

    // Test que debería fallar: Crear un cliente sin actividades.
    @Test
    void crearClienteSinActividades_falla() {
        Cliente cliente = new Cliente("Juan", "Perez", "124", "111");
        assertThrows(RuntimeException.class, () ->
            clienteService.guardarOActualizarCliente(
                cliente,
                List.of(),
                LocalDate.now(),
                TipoDeCobro.MENSUAL
            )
        );
    }
    // Test que debería fallar: Crear un cliente y posteriormente otro con el mismo DNI.
    @Test
    void crearClienteDniDuplicado_falla() {
        Cliente c1 = new Cliente("Juan", "Perez", "125", "111");

        clienteService.guardarOActualizarCliente(
            c1,
            List.of(gym.getIdActividad()),
            LocalDate.now(),
            TipoDeCobro.MENSUAL
        );

        Cliente c2 = new Cliente("Pedro", "Lopez", "125", "222");

        assertThrows(RuntimeException.class, () ->
            clienteService.guardarOActualizarCliente(
                c2,
                List.of(gym.getIdActividad()),
                LocalDate.now(),
                TipoDeCobro.MENSUAL
            )
        );
    }
    // TEST DE EDICIÓN 
    @Test
    void editarDatosCliente_ok() {
        Cliente cliente = new Cliente("Juan", "Perez", "126", "111");

        clienteService.guardarOActualizarCliente(
            cliente,
            List.of(gym.getIdActividad()),
            LocalDate.now(),
            TipoDeCobro.MENSUAL
        );
        // Obtenemos desde la base de datos el cliente
        Cliente db = clienteRepository.findAll().get(0);
        // Estos serían los datos traídos desde el formulario (la edición)
        Cliente form = new Cliente();
        form.setIdCliente(db.getIdCliente());
        form.setNombre("Piter Carlos");
        form.setApellido("Popo");
        form.setDni("126");
        form.setTelefono("999");

        clienteService.guardarOActualizarCliente(
            form,
            List.of(gym.getIdActividad()),
            LocalDate.now(),
            TipoDeCobro.MENSUAL
        );

        Cliente actualizado = clienteRepository.findById(db.getIdCliente()).get();
        assertEquals("Piter Carlos", actualizado.getNombre());
        assertEquals("Popo", actualizado.getApellido());
    
    }
    @Test
    void editar_quitarActividad_laDaDeBaja() {
        Cliente cliente = new Cliente("Juan", "Perez", "127", "111");

        clienteService.guardarOActualizarCliente(
            cliente,
            List.of(gym.getIdActividad(), boxeo.getIdActividad()),
            LocalDate.now(),
            TipoDeCobro.MENSUAL
        );

        Cliente db = clienteRepository.findAll().get(0);

        Cliente form = new Cliente();
        form.setIdCliente(db.getIdCliente());
        form.setIdCliente(db.getIdCliente());
        form.setNombre(db.getNombre());
        form.setApellido(db.getApellido());
        form.setDni(db.getDni());
        form.setTelefono(db.getTelefono());
        form.setObservaciones(db.getObservaciones());
        
        clienteService.guardarOActualizarCliente(
            form,
            List.of(gym.getIdActividad()), // quitamos boxeo
            LocalDate.now(),
            TipoDeCobro.MENSUAL
        );

        long activas = db.getInscripciones().stream()
            .filter(i -> i.getEstado() == EstadoInscripcion.ACTIVA)
            .count();

        assertEquals(1, activas);
    }
}