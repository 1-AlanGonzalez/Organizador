package com.gymmanager.gym_manager.services.exports;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

public class ExcelGenerator {
    public static byte[] generarExcel(String nombreHoja, List<Map<String, Object>> datos) {

        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet(nombreHoja);

            if (datos.isEmpty()) {
                return new byte[0];
            }

            // 🔥 Crear encabezados dinámicos
            Row headerRow = sheet.createRow(0);
            Map<String, Object> primeraFila = datos.get(0);

            int colIndex = 0;
            for (String columna : primeraFila.keySet()) {
                Cell cell = headerRow.createCell(colIndex++);
                cell.setCellValue(columna);
            }

            // 🔥 Crear filas
            int rowIndex = 1;
            for (Map<String, Object> filaData : datos) {

                Row row = sheet.createRow(rowIndex++);
                colIndex = 0;

                for (Object valor : filaData.values()) {

                    Cell cell = row.createCell(colIndex++);

                    if (valor instanceof Number) {
                        cell.setCellValue(((Number) valor).doubleValue());
                    } else if (valor != null) {
                        cell.setCellValue(valor.toString());
                    } else {
                        cell.setCellValue("");
                    }
                }
            }

            // Ajustar columnas
            for (int i = 0; i < primeraFila.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);

            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generando Excel", e);
        }
    }
}
