package com.gymmanager.gym_manager.services;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.gymmanager.gym_manager.entity.Actividad;
import com.gymmanager.gym_manager.entity.ActividadCliente;
import com.gymmanager.gym_manager.entity.Cliente;
import com.gymmanager.gym_manager.entity.EstadoInscripcion;
import com.gymmanager.gym_manager.entity.MetodoDePago;
import com.gymmanager.gym_manager.entity.TipoDeCobro;
import com.gymmanager.gym_manager.entity.Usuario;
import com.gymmanager.gym_manager.repository.ClienteActividadRepository;
import com.gymmanager.gym_manager.repository.MetodoDePagoRepository;


/*
  Service de inscripción de clientes a actividades.
 
  Centra la lógica relacionada con:
  - Inscribir clientes
  - Validar cupos
  - Evitar inscripciones duplicadas
  - Dar de baja inscripciones
 
  Este service relaciona/coordina entidades (Cliente, Actividad, Inscripción) y utiliza el Repository solo para acceder a la base de datos (hacer consultas).
 
  No mezcla lorgica relacionada con los controllers que es la capa superior.
 */

@Service
public class ActividadClienteService {
    private final ClienteActividadRepository actividadClienteRepository;
    private final MetodoDePagoRepository metodoDePagoRepository;

    public ActividadClienteService(ClienteActividadRepository actividadClienteRepository, MetodoDePagoRepository metodoDePagoRepository){
        this.actividadClienteRepository = actividadClienteRepository;
        this.metodoDePagoRepository = metodoDePagoRepository;
    }


    /*
     Inscribe un cliente a una actividad.
    
        Lo que hace:
        - El cliente no puede estar ya inscripto de forma activa
        - La actividad no puede superar su cupo máximo
        Si las validaciones pasan, se crea una nueva inscripción activa y la añade a el conjunto de inscripciones del cliente (entidad)
    */

     public ActividadCliente inscribirCliente(Cliente cliente, Actividad actividad,
                                              LocalDate fechaInicio, TipoDeCobro tipoCobro,
                                              Usuario usuario) {
         boolean yaEstaInscripto = actividadClienteRepository
                    .existsByClienteAndActividadAndEstado(cliente, actividad, EstadoInscripcion.ACTIVA);
        if(yaEstaInscripto){
            throw new RuntimeException("El cliente ya esta inscripto en esta actividad");
        }

        int inscriptosActuales = actividadClienteRepository.countByActividadAndEstado(actividad, EstadoInscripcion.ACTIVA);
        if(inscriptosActuales >= actividad.getCupoMaximo()){
            throw new RuntimeException("No hay cupos disponibles para esta actividad");
        }
  

        BigDecimal costo;
        if (tipoCobro == TipoDeCobro.DIARIO) {
            costo = actividad.getPrecioDiario() != null
                    ? actividad.getPrecioDiario()
                    : actividad.getPrecio();
        } else {
            costo = actividad.getPrecio();
        }

        ActividadCliente inscripcion = new ActividadCliente(fechaInicio, costo, cliente, actividad, tipoCobro);

        cliente.agregarInscripcion(inscripcion); 
        
        MetodoDePago metodoPago = metodoDePagoRepository
                        .findByNombreAndUsuario("No especificado", usuario)
                        .orElseThrow(() -> new RuntimeException(
                                "Método de pago 'NO_ESPECIFICADO' no encontrado para este usuario."));

        inscripcion.generarPago(fechaInicio,metodoPago);

        return actividadClienteRepository.save(inscripcion);
    }

    /*
        Da de baja una inscripción activa.
 
        No elimina registros: cambia el estado de la inscripción a BAJA para conservar el historial de inscripciones totales.
 
        Lanza excepción si el cliente no tiene una inscripción activa.
    */

    public ActividadCliente darseDeBaja(Cliente cliente, Actividad actividad){
        ActividadCliente inscripcion = actividadClienteRepository.findByClienteAndActividadAndEstado(cliente, actividad, EstadoInscripcion.ACTIVA).orElseThrow(()-> new RuntimeException("El cliente no tiene una inscripción activa en esta actividad"));
        
        cliente.darseDeBajaAInscripcion(inscripcion);
        return actividadClienteRepository.save(inscripcion);

    }
}
