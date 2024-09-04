package org.example;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class JEUI {

    public static void dataforJEUI(String filePath, String sheetName, List<Map.Entry<String, List<String>>> selectedEntries, double amount) {
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            // Create a new sheet
            Sheet sheet = workbook.createSheet(sheetName);

            // Populate the sheet with data
            int rowIndex = 0;
            for (int i = 0; i < selectedEntries.size(); i++) {
                Row row = sheet.createRow(rowIndex++);

                // Column 1: ID (incremented by 1)
                row.createCell(0).setCellValue(i + 1);

                // Column 2: Key (from selected entries list)
                row.createCell(1).setCellValue(selectedEntries.get(i).getKey());

                // Column 3: Amount (assigned to each key)
                row.createCell(2).setCellValue(amount);

                // Column 4: Value (Cr/Dr from the selected entries at index 0)
                row.createCell(3).setCellValue(selectedEntries.get(i).getValue().get(0));
            }

            // Write the data to the same Excel file
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
