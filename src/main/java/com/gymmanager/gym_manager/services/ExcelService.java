package com.gymmanager.gym_manager.services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;


@Service
public class ExcelService {

    private String formatearHeader(String campo) {

        String[] partes = campo.split("\\.");
        String ultimo = partes[partes.length - 1];

        String separado = ultimo.replaceAll("([a-z])([A-Z])", "$1 $2");

        return separado.substring(0, 1).toUpperCase() + separado.substring(1);
    }

    public byte[] generarExcel(Map<String, List<Map<String, Object>>> datos) {

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // 🔁 Recorremos cada hoja
            for (Map.Entry<String, List<Map<String, Object>>> entry : datos.entrySet()) {

                String nombreHoja = entry.getKey();
                List<Map<String, Object>> filas = entry.getValue();

                Sheet sheet = workbook.createSheet(nombreHoja);

                if (filas == null || filas.isEmpty()) {
                    continue;
                }

                Row headerRow = sheet.createRow(0);

                Map<String, Object> primeraFila = filas.get(0);
                int colIndex = 0;

                // 🔹 Headers
                for (String columna : primeraFila.keySet()) {
                    Cell cell = headerRow.createCell(colIndex++);
                    cell.setCellValue(formatearHeader(columna));
                }

                int rowIndex = 1;

                // 🔹 Filas
                for (Map<String, Object> fila : filas) {

                    Row row = sheet.createRow(rowIndex++);
                    colIndex = 0;

                    for (Object valor : fila.values()) {

                        Cell cell = row.createCell(colIndex++);

                        if (valor instanceof Number) {
                            cell.setCellValue(((Number) valor).doubleValue());
                        } else if (valor != null) {
                            cell.setCellValue(valor.toString());
                        }
                    }
                }

                // 🔹 Ajustar columnas
                for (int i = 0; i < primeraFila.size(); i++) {
                    sheet.autoSizeColumn(i);
                    
                    int anchoActual = sheet.getColumnWidth(i);
                    int anchoConMargen = (int) (anchoActual * 1.3);
                    int anchoMinimo = 4000;
                    
                    sheet.setColumnWidth(i, Math.max(anchoConMargen, anchoMinimo));
                }
            }

            workbook.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Error generando Excel", e);
        }
    }
}


