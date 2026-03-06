package com.gymmanager.gym_manager.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gymmanager.gym_manager.entity.dto.ExportRequest;
import com.gymmanager.gym_manager.services.ExcelService;
import com.gymmanager.gym_manager.services.ExportService;

@RestController
@RequestMapping("/exportar")
public class ExportarController {

    private final ExportService exportService;
    private final ExcelService excelService;

    public ExportarController(ExportService exportService,
                              ExcelService excelService) {
        this.exportService = exportService;
        this.excelService = excelService;
    }

    @PostMapping("/excel")
    public ResponseEntity<byte[]> exportarExcel(@RequestBody ExportRequest request) {

    Map<String, List<Map<String, Object>>> datos =
            exportService.generarExportacion(request);
    System.out.println("Hojas que se van a crear: " + datos.keySet());
    byte[] archivo = excelService.generarExcel(datos);

    return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=export.xlsx")
            .header("Content-Type", "application/octet-stream")
            .body(archivo);
}
}
