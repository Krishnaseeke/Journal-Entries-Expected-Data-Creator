package org.example;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class COAImpact {

    public static void calculateAndCreateImpactSheet(String filePath, List<Map.Entry<String, List<String>>> selectedEntries, List<Map.Entry<String, List<String>>> jeEntries) {
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            // Clear or create the "Impact" sheet
            Sheet impactSheet = workbook.getSheet("Impact");
            if (impactSheet == null) {
                impactSheet = workbook.createSheet("Impact");
            } else {
                // Clear existing content
                int lastRow = impactSheet.getLastRowNum();
                for (int i = 0; i <= lastRow; i++) {
                    Row row = impactSheet.getRow(i);
                    if (row != null) {
                        impactSheet.removeRow(row);
                    }
                }
            }

            // Header row
            Row headerRow = impactSheet.createRow(0);
            headerRow.createCell(0).setCellValue("Account Name");
            headerRow.createCell(1).setCellValue("Current Balance");
            headerRow.createCell(2).setCellValue("Amount Type");

            // Process entries
            for (Map.Entry<String, List<String>> selectedEntry : selectedEntries) {
                String selectedKey = selectedEntry.getKey();
                List<String> selectedValues = selectedEntry.getValue();

                // Find matching key in jeEntries
                for (Map.Entry<String, List<String>> jeEntry : jeEntries) {
                    if (selectedKey.equals(jeEntry.getKey())) {
                        List<String> jeValues = jeEntry.getValue();

                        // Get the Dr/Cr values
                        String selectedCrDr = selectedValues.get(1).trim();
                        String jeCrDr = jeValues.get(1).trim();

                        // Get the numeric values
                        double selectedAmount = parseDoubleOrZero(selectedValues.get(0).trim());
                        double jeAmount = parseDoubleOrZero(jeValues.get(0).trim());

                        // Update the selected entries based on matching Cr/Dr
                        if (selectedCrDr.equals(jeCrDr)) {
                            selectedAmount += jeAmount; // Add if Cr/Dr match
                        } else {
                            selectedAmount -= jeAmount; // Subtract if Cr/Dr do not match
                        }

                        // Adjust Cr/Dr if the amount becomes negative
                        if (selectedAmount < 0) {
                            selectedAmount = Math.abs(selectedAmount);
                            selectedCrDr = selectedCrDr.equals("Cr") ? "Dr" : "Cr";
                        }

                        // Update selectedValues with new amounts and Cr/Dr
                        selectedValues.set(0, String.valueOf(selectedAmount));
                        selectedValues.set(1, selectedCrDr);

                        break; // Stop checking further jeEntries for this selectedEntry
                    }
                }
            }

            // Populate the "Impact" sheet with updated selectedEntries
            int rowIndex = 1; // Start after header
            for (Map.Entry<String, List<String>> entry : selectedEntries) {
                Row row = impactSheet.createRow(rowIndex++);
                List<String> values = entry.getValue();

                row.createCell(0).setCellValue(entry.getKey()); // Column 1: Account Name
                row.createCell(1).setCellValue(parseDoubleOrZero(values.get(0))); // Column 2: Current Balance
                row.createCell(2).setCellValue(values.get(1)); // Column 3: Amount Type (Cr/Dr)
            }

            // Write back to the same Excel file
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static double parseDoubleOrZero(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0; // Return 0 if the value cannot be parsed as a double
        }
    }
}
