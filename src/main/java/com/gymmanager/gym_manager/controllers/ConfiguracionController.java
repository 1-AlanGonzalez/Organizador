package com.gymmanager.gym_manager.controllers;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gymmanager.gym_manager.entity.Configuracion;
import com.gymmanager.gym_manager.services.ConfiguracionService;
import com.gymmanager.gym_manager.services.ExcelService;

import jakarta.servlet.http.HttpServletResponse;


@Controller
@RequestMapping("/configuracion")
public class ConfiguracionController {
    private final ConfiguracionService configuracionService;
    private final ExcelService excelService;

    
    public ConfiguracionController(ConfiguracionService configuracionService, ExcelService excelService) {
        this.configuracionService = configuracionService;
        this.excelService = excelService;
    }

    @GetMapping
    public String configuracion(Model model) {
        model.addAttribute("config", new Configuracion());

        model.addAttribute("title", "Gym Manager | Configuración");
        model.addAttribute("header", "Panel de control / Configuración");

        model.addAttribute("vista", "configuracion/configuracion");
        model.addAttribute("fragmento", "contenido");

        model.addAttribute("active", "configuracion");

        return "layouts/main";
    }

    @PostMapping("/cambiar-recargo")
    public String cambiarPorcentaje(
        @RequestParam Integer idConfiguracion,
        @RequestParam BigDecimal porcentaje,
        Model model,
        RedirectAttributes redirectAttributes){
            try{

                configuracionService.cambiarPorcentajeInteres(idConfiguracion, porcentaje);
                redirectAttributes.addFlashAttribute("succes","Porcentaje de recargo actualizado correctamente");
            }
            catch(Exception e ){
                redirectAttributes.addFlashAttribute("error",e.getMessage());
            }
             return "redirect:/configuracion";
        }
    
    @GetMapping("/exportar")
    public void exportar(
        @RequestParam String entidad,
        @RequestParam List<String> columnas,
        HttpServletResponse response
    ) throws IOException {

        // Esto le dice al navegador que descargue esto como archivo
        // usamos response para escribir directamente el archivo en la respuesta.

        // Esto quiere decir que manda un archivo binario.
        // Basicamente dice, No lo muestres en pantalla, es un archivo para descargar.
        response.setContentType("application/octet-stream");
        
        //Le dice al navegador: 
        // Esto es un archivo adjunto 
        // Descargalo 
        // Y ponelo con este nombre
        response.setHeader("Content-Disposition",
            "attachment; filename=" + entidad.toLowerCase() + ".xlsx");

        excelService.exportar(entidad, columnas, response);

    }
}