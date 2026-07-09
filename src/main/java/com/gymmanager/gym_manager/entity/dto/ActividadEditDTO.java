package com.gymmanager.gym_manager.entity.dto;

import java.math.BigDecimal;
import java.util.List;

public record ActividadEditDTO(
        Integer        id,
        String         nombre,
        BigDecimal     precio,
        BigDecimal     precioDiario,
        Integer        cupoMaximo,
        List<DictaDTO> instructores
) {}