package com.gymmanager.gym_manager.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gymmanager.gym_manager.entity.Actividad;
import com.gymmanager.gym_manager.entity.ActividadCliente;
import com.gymmanager.gym_manager.entity.Cliente;
import com.gymmanager.gym_manager.entity.EstadoInscripcion;

public interface ClienteActividadRepository extends JpaRepository<ActividadCliente, Integer> {

    /*
     Repository de ActividadCliente (Inscripción).

        Su responsabilidad es únicamente acceder a la base de datos.
        No se hace logica aca
 
    Se usa para:
        - Consultar si un cliente ya está inscripto a una actividad
        - Contar inscripciones activas (cupo)
        - Obtener una inscripción puntual para modificarla (baja, pagos, etc.)
 
    */
    
    Integer countByActividadAndEstado(Actividad actividad, EstadoInscripcion estado);

    boolean existsByClienteAndActividadAndEstado(Cliente cliente,Actividad actividad,EstadoInscripcion estado);

    Optional<ActividadCliente> findByClienteAndActividadAndEstado(Cliente cliente, Actividad actividad, EstadoInscripcion estado);

    // Te trae todas las incripciones en estado Activo.
    @Query("SELECT ac FROM ActividadCliente ac WHERE ac.estado = 'ACTIVA'")
    List<ActividadCliente> findAllActivas();

}


/*
  Repositories:
  - Hablan con la base de datos
  - No tienen lógica
 
  Services:
  - Contienen las reglas del sistema
  - Validan, coordinan entidades y usan repositories
 
  Entidades:
  - Representan el dominio
  - Contienen comportamiento propio y logica simple del mismo
 */
