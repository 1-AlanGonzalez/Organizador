package com.gymmanager.gym_manager.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ── 404: recursos estáticos que no existen (favicon, devtools, etc.) ──────
    // El navegador los pide automáticamente — no son errores reales, se ignoran
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void handleNoResource() {
        // Sin log, sin página de error — simplemente devuelve 404
    }

    // ── 403: acceso denegado ──────────────────────────────────────────────────
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleAccesDenied(Model model) {
        model.addAttribute("codigo",  "403");
        model.addAttribute("titulo",  "Acceso denegado");
        model.addAttribute("mensaje", "No tenés permiso para acceder a esta sección.");
        return "error";
    }

    // ── 500: cualquier otro error inesperado ──────────────────────────────────
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleError(Exception e, Model model) {
        log.error("Error inesperado: {}", e.getMessage(), e);
        model.addAttribute("codigo",  "500");
        model.addAttribute("titulo",  "Algo salió mal");
        model.addAttribute("mensaje", "Ocurrió un error inesperado. Por favor, intentá de nuevo.");
        return "error";
    }
}