package com.gymmanager.gym_manager.entity.dto;

import java.math.BigDecimal;

public record DeudaDTO(Integer idActividadCliente, String actividad, BigDecimal montoAdeudado) {}
