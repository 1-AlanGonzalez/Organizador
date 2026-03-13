package com.gymmanager.gym_manager.controllers;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gymmanager.gym_manager.config.SecurityUtils;
import com.gymmanager.gym_manager.entity.Configuracion;
import com.gymmanager.gym_manager.entity.Usuario;
import com.gymmanager.gym_manager.repository.UsuarioRepository;
import com.gymmanager.gym_manager.services.ConfiguracionDePagoService;

@Controller
@RequestMapping("/configuracion")
public class ConfiguracionController {

    private final ConfiguracionDePagoService configuracionDePagoService;
    private final SecurityUtils  securityUtils;
    private final UsuarioRepository usuarioRepository;

    public ConfiguracionController(ConfiguracionDePagoService configuracionDePagoService,
                                   SecurityUtils  securityUtils,
                                   UsuarioRepository usuarioRepository) {
        this.configuracionDePagoService = configuracionDePagoService;
        this.securityUtils              = securityUtils;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public String configuracion(Model model) {
        Usuario usuario = securityUtils.getUsuarioActual();
        var activos   = configuracionDePagoService.listarActivos(usuario);
        var inactivos = configuracionDePagoService.listarInactivos(usuario);

        Configuracion config = new Configuracion();
        config.setNombreGimnasio(usuario.getNombreGimnasio()); 
        config.setDireccion(usuario.getDireccion());
        config.setTelefono(usuario.getTelefono());
        config.setMensajeTicket(usuario.getMensajeTicket());

        model.addAttribute("config", config);
        model.addAttribute("metodosDePago",    activos);
        model.addAttribute("metodosVacios",    activos.isEmpty());
        model.addAttribute("metodosInactivos", inactivos);
        model.addAttribute("usuario",          usuario);  // ← necesario para la tab Mi Cuenta

        model.addAttribute("title",     "Gym Manager | Configuración");
        model.addAttribute("header",    "Panel de control / Configuración");
        model.addAttribute("vista",     "configuracion/configuracion");
        model.addAttribute("fragmento", "contenido");
        model.addAttribute("active",    "configuracion");
        return "layouts/main";
    }

   // POST /tickets — guardar en el usuario:
    @PostMapping("/guardar/tickets")
    public String guardarTickets(@ModelAttribute("config") Configuracion config,
                                RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = securityUtils.getUsuarioActual();
            usuario.setNombreGimnasio(config.getNombreGimnasio());
            usuario.setDireccion(config.getDireccion()); 
            usuario.setTelefono(config.getTelefono());   
            usuario.setMensajeTicket(config.getMensajeTicket());
            
            usuarioRepository.save(usuario);
            redirectAttributes.addFlashAttribute("success", "Configuración guardada.");
            redirectAttributes.addFlashAttribute("tabActivo", "tickets");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tabActivo", "tickets");
        }
        return "redirect:/configuracion";
    }

    @PostMapping("/guardar/pagos")
    public String guardarPagos(
            @RequestParam(value = "configId",      required = false) List<Integer>    configIds,
            @RequestParam(value = "configRecargo", required = false) List<BigDecimal> configRecargoList,
            @RequestParam(value = "nuevoNombre",   required = false) List<String>     nuevosNombres,
            @RequestParam(value = "nuevoRecargo",  required = false) List<BigDecimal> nuevosRecargoList,
            RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = securityUtils.getUsuarioActual();

            if (configIds != null) {
                for (int i = 0; i < configIds.size(); i++) {
                    configuracionDePagoService.actualizarRecargo(
                            configIds.get(i),
                            safeGet(configRecargoList, i, BigDecimal.ZERO));
                }
            }

            if (nuevosNombres != null) {
                for (int i = 0; i < nuevosNombres.size(); i++) {
                    String nombre = nuevosNombres.get(i);
                    if (nombre == null || nombre.isBlank()) continue;
                    configuracionDePagoService.crearMetodoConRecargo(
                            nombre,
                            safeGet(nuevosRecargoList, i, BigDecimal.ZERO),
                            usuario);
                }
            }

            redirectAttributes.addFlashAttribute("success", "Métodos de pago guardados correctamente.");
            redirectAttributes.addFlashAttribute("tabActivo", "pagos");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("tabActivo", "pagos");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al guardar: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tabActivo", "pagos");
        }
        return "redirect:/configuracion";
    }

    @DeleteMapping("/metodos/{idMetodo}")
    @ResponseBody
    public ResponseEntity<Void> desactivarMetodo(@PathVariable Integer idMetodo) {
        try {
            configuracionDePagoService.desactivarMetodo(idMetodo);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(409).build();
        }
    }

    @PatchMapping("/metodos/{idMetodo}/reactivar")
    @ResponseBody
    public ResponseEntity<Void> reactivarMetodo(@PathVariable Integer idMetodo) {
        try {
            configuracionDePagoService.reactivarMetodo(idMetodo);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(409).build();
        }
    }

    @DeleteMapping("/metodos/{idMetodo}/permanente")
    @ResponseBody
    public ResponseEntity<Void> eliminarPermanente(@PathVariable Integer idMetodo) {
        try {
            configuracionDePagoService.eliminarMetodoPermanente(idMetodo);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(409).build();
        }
    }

    private <T> T safeGet(List<T> list, int index, T defaultValue) {
        if (list == null || index >= list.size() || list.get(index) == null)
            return defaultValue;
        return list.get(index);
    }


    
}