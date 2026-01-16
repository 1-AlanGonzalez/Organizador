package com.gymmanager.gym_manager.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gymmanager.gym_manager.entity.ActividadCliente;

public interface ClienteActividadRepository extends JpaRepository<ActividadCliente, Integer> {

}
