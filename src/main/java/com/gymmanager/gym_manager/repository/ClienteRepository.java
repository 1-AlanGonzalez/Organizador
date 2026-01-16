package com.gymmanager.gym_manager.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gymmanager.gym_manager.entity.Cliente;



public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

}