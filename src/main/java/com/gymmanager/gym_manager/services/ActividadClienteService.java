package com.gymmanager.gym_manager.services;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.gymmanager.gym_manager.entity.Actividad;
import com.gymmanager.gym_manager.entity.ActividadCliente;
import com.gymmanager.gym_manager.entity.Cliente;
import com.gymmanager.gym_manager.entity.EstadoInscripcion;
import com.gymmanager.gym_manager.repository.ClienteActividadRepository;

@Service
public class ActividadClienteService {
    private final ClienteActividadRepository actividadClienteRepository;
    

    public ActividadClienteService(ClienteActividadRepository actividadClienteRepository){
        this.actividadClienteRepository = actividadClienteRepository;
    }

    public ActividadCliente inscribirCliente(Cliente cliente, Actividad actividad){
        boolean yaEstaInscripto = actividadClienteRepository.existsByClienteAndActividadAndEstado(cliente, actividad, EstadoInscripcion.ACTIVA);

        if(yaEstaInscripto){
            throw new RuntimeException("El cliente ya esta inscripto en esta actividad");
        }

        int inscriptosActuales = actividadClienteRepository.countByActividadAndEstado(actividad, EstadoInscripcion.ACTIVA);
        if(inscriptosActuales >= actividad.getCupoMaximo()){
            throw new RuntimeException("No hay cupos disponibles para esta actividad");
        }

        ActividadCliente inscripcion = new ActividadCliente(LocalDate.now(), actividad.getPrecio(), cliente, actividad);

        cliente.agregarInscripcion(inscripcion);

        return actividadClienteRepository.save(inscripcion);
    }

    public ActividadCliente darseDeBaja(Cliente cliente, Actividad actividad){
        ActividadCliente inscripcion = actividadClienteRepository.findByClienteAndActividadAndEstado(cliente, actividad, EstadoInscripcion.ACTIVA).orElseThrow(()-> new RuntimeException("El cliente no tiene una inscripción activa en esta actividad"));
        
        cliente.darseDeBajaAInscripcion(inscripcion);

        return actividadClienteRepository.save(inscripcion);

    }
}
