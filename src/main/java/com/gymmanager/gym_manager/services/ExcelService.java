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

    // Nos quedamos con lo que está después del último punto
    String[] partes = campo.split("\\.");
    String ultimo = partes[partes.length - 1];

    // Separar camelCase → "metodoPago" → "metodo Pago"
    String separado = ultimo.replaceAll("([a-z])([A-Z])", "$1 $2");

    // Primera letra mayúscula
    return separado.substring(0, 1).toUpperCase() 
           + separado.substring(1);
}
    public byte[] generarExcel(Map<String, List<Map<String, Object>>> datos) {

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // 🔁 Recorremos cada hoja
            for (String nombreHoja : datos.keySet()) {

                Sheet sheet = workbook.createSheet(nombreHoja);

                List<Map<String, Object>> filas = datos.get(nombreHoja);

                if (filas == null || filas.isEmpty()) {
                    continue;
                }

                // 🟢 Crear encabezados (headers)
                Row headerRow = sheet.createRow(0);

                Map<String, Object> primeraFila = filas.get(0);
                int colIndex = 0;

                for (String columna : primeraFila.keySet()) {
                    Cell cell = headerRow.createCell(colIndex++);
                    cell.setCellValue(formatearHeader(columna));
                }

                // 🟢 Crear datos
                int rowIndex = 1;

                for (Map<String, Object> fila : filas) {

                    Row row = sheet.createRow(rowIndex++);
                    colIndex = 0;

                    for (Object valor : fila.values()) {

                        Cell cell = row.createCell(colIndex++);

                        if (valor != null) {
                            cell.setCellValue(valor.toString());
                        }
                    }
                }
            }

            workbook.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Error generando Excel", e);
        }
    }
}


