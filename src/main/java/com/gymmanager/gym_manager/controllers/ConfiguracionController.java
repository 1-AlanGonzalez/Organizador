package com.gymmanager.gym_manager.controllers;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gymmanager.gym_manager.entity.Configuracion;
import com.gymmanager.gym_manager.services.ConfiguracionDePagoService;
import com.gymmanager.gym_manager.services.ConfiguracionService;
import com.gymmanager.gym_manager.services.ExcelService;

import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/configuracion")
public class ConfiguracionController {

    private final ConfiguracionDePagoService configuracionDePagoService;
    private final ExcelService excelService;

    public ConfiguracionController(ConfiguracionService configuracionService,
                                   ConfiguracionDePagoService configuracionDePagoService,
                                   ExcelService excelService) {
        this.configuracionDePagoService  = configuracionDePagoService;
        this.excelService         = excelService;
    }

    @GetMapping
    public String configuracion(Model model) {
        var metodos = configuracionDePagoService.listarActivos();  

        model.addAttribute("config",        new Configuracion());
        model.addAttribute("metodosDePago", metodos);
        model.addAttribute("metodosVacios", metodos.isEmpty());

        model.addAttribute("title",     "Gym Manager | Configuración");
        model.addAttribute("header",    "Panel de control / Configuración");
        model.addAttribute("vista",     "configuracion/configuracion");
        model.addAttribute("fragmento", "contenido");
        model.addAttribute("active",    "configuracion");

        return "layouts/main";
    }

    @PostMapping("/guardar")
    public String guardar(
            @ModelAttribute("config") Configuracion config,

            @RequestParam(value = "configId",      required = false) List<Integer>    configIds,
            @RequestParam(value = "configRecargo", required = false) List<BigDecimal> configRecargoList,

            @RequestParam(value = "nuevoNombre",  required = false) List<String>     nuevosNombres,
            @RequestParam(value = "nuevoRecargo", required = false) List<BigDecimal> nuevosRecargoList,

            RedirectAttributes redirectAttributes) {

        try {
            // 2. Actualizar recargos de configuraciones existentes
            if (configIds != null) {
                for (int i = 0; i < configIds.size(); i++) {
                    BigDecimal recargo = safeGet(configRecargoList, i, BigDecimal.ZERO);
                    configuracionDePagoService.actualizarRecargo(configIds.get(i), recargo);
                }
            }

            // 3. Crear nuevos métodos de pago
            if (nuevosNombres != null) {
                for (int i = 0; i < nuevosNombres.size(); i++) {
                    String nombre = nuevosNombres.get(i);
                    if (nombre == null || nombre.isBlank()) continue;
                    BigDecimal recargo = safeGet(nuevosRecargoList, i, BigDecimal.ZERO);
                    configuracionDePagoService.crearMetodoConRecargo(nombre, recargo);
                }
            }

            redirectAttributes.addFlashAttribute("success", "Cambios guardados correctamente.");

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al guardar: " + e.getMessage());
        }

        return "redirect:/configuracion";
    }

    // SISTEMA DE TICKETS ENDPOINT

    @PostMapping("/tickets")
    public String guardarTickets(
            @ModelAttribute("config") Configuracion config,
            RedirectAttributes redirectAttributes) {

        try {
            // configuracionService.guardar(config);

            redirectAttributes.addFlashAttribute("success", "Configuración guardada.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }

        return "redirect:/configuracion";
    }
    // ENDPOINT PARA PAGOS
    // 
    // 
    // 

    
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

    @GetMapping("/exportar")
    public void exportar(
            @RequestParam String       entidad,
            @RequestParam List<String> columnas,
            HttpServletResponse        response) throws IOException {

        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition",
                "attachment; filename=" + entidad.toLowerCase() + ".xlsx");

        excelService.exportar(entidad, columnas, response);
    }

    private <T> T safeGet(List<T> list, int index, T defaultValue) {
        if (list == null || index >= list.size() || list.get(index) == null)
            return defaultValue;
        return list.get(index);
    }
}