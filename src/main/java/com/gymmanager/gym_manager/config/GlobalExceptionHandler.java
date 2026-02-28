package com.gymmanager.gym_manager.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ── 403: el usuario no tiene permiso (ej: USER intenta entrar a /usuarios) ─
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleAccesDenied(AccessDeniedException e, Model model) {
        model.addAttribute("codigo",  "403");
        model.addAttribute("titulo",  "Acceso denegado");
        model.addAttribute("mensaje", "No tenés permiso para acceder a esta sección.");
        return "error";
    }

    // ── 500: cualquier error inesperado ───────────────────────────────────────
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleError(Exception e, Model model) {
        // El error real se loguea en el servidor para debugging
        log.error("Error inesperado: {}", e.getMessage(), e);

        // Al usuario le mostramos un mensaje genérico, sin detalles técnicos
        model.addAttribute("codigo",  "500");
        model.addAttribute("titulo",  "Algo salió mal");
        model.addAttribute("mensaje", "Ocurrió un error inesperado. Por favor, intentá de nuevo.");
        return "error";
    }
}