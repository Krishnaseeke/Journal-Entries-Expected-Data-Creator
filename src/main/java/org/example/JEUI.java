package org.example;


import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class JEUI {

    public static void dataforJEUI(String filePath, List<Map.Entry<String, List<String>>> selectedEntries) {
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet("JE-UI");

            // Check if JE-UI sheet exists; if not, create it.
            if (sheet != null) {
                // Clear the existing sheet
                for (int i = sheet.getLastRowNum(); i >= 0; i--) {
                    Row row = sheet.getRow(i);
                    if (row != null) {
                        sheet.removeRow(row); // Only remove if the row exists
                    }
                }
            } else {
                // Create the JE-UI sheet
                sheet = workbook.createSheet("JE-UI");
            }

            // Add headers
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Serial No");
            headerRow.createCell(1).setCellValue("Accounts");
            headerRow.createCell(2).setCellValue("Credit Amount");
            headerRow.createCell(3).setCellValue("Debit Amount");

            // Populate the sheet with data
            int rowIndex = 1; // Start from row 1, since row 0 is for headers
            for (int i = 0; i < selectedEntries.size(); i++) {
                Map.Entry<String, List<String>> entry = selectedEntries.get(i);
                String key = entry.getKey();
                String valueType = entry.getValue().get(1); // Check Cr/Dr at index 1
                String valueAmount = entry.getValue().get(0); // Amount at index 0

                Row row = sheet.createRow(rowIndex++);

                // Column 1: Serial No (incremented by 1)
                row.createCell(0).setCellValue(i + 1);

                // Column 2: Account Name (selectedEntries.getKey())
                row.createCell(1).setCellValue(key);

                // Column 3: Credit Amount (only if Cr is present)
                if ("Cr".equalsIgnoreCase(valueType)) {
                    row.createCell(2).setCellValue(valueAmount);
                } else {
                    row.createCell(2).setCellValue(""); // Leave blank if not Cr
                }

                // Column 4: Debit Amount (only if Dr is present)
                if ("Dr".equalsIgnoreCase(valueType)) {
                    row.createCell(3).setCellValue(valueAmount);
                } else {
                    row.createCell(3).setCellValue(""); // Leave blank if not Dr
                }
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

