package org.example;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class COAImpact {

    public static void calculateAndCreateImpactSheet(String filePath, List<Map.Entry<String, List<String>>> selectedEntries, double amount) {
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            // Create a new sheet for impact analysis
            Sheet impactSheet = workbook.createSheet("Impact");

            // Row 0: COA Impact
            Row headerRow = impactSheet.createRow(0);
            Cell headerCell = headerRow.createCell(0);
            headerCell.setCellValue("COA Impact");

            // Populate the sheet with data
            int rowIndex = 1; // Start after the header row
            for (Map.Entry<String, List<String>> entry : selectedEntries) {
                List<String> values = entry.getValue();

                // Check if the values contain "Chart of Accounts"
                if (!values.stream().anyMatch(val -> val.contains("Chart of Accounts"))) {
                    continue; // Skip this entry if "Chart of Accounts" is not found
                }

                Row row = impactSheet.createRow(rowIndex++);

                String key = entry.getKey();

                // Validate and parse Value[Index 1] and Value[Index 0]
                String valueStr1 = values.get(1).trim();
                String valueStr0 = values.get(0).trim();

                if (valueStr1.isEmpty() || valueStr0.isEmpty()) {
                    System.out.println("Skipping entry with empty value for key: " + key);
                    continue; // Skip processing if values are empty
                }

                double valueAtIndex1 = parseDoubleOrZero(valueStr1);
                double valueAtIndex0 = parseDoubleOrZero(valueStr0.replace("Cr", "").replace("Dr", "").trim());

                String crDr = valueStr0.contains("Cr") ? "Cr" : "Dr";

                // Calculate the impact based on Cr/Dr
                if (crDr.equals("Cr")) {
                    valueAtIndex1 -= amount;
                } else if (crDr.equals("Dr")) {
                    valueAtIndex1 += amount;
                }

                // Column 2: Key
                row.createCell(1).setCellValue(key);

                // Column 3: Value[Index 1] after the above logic calculation
                row.createCell(2).setCellValue(valueAtIndex1);

                // Reverse Cr/Dr if Value[Index 0] is negative
                if (valueAtIndex1 < 0) {
                    crDr = crDr.equals("Cr") ? "Dr" : "Cr"; // Reverse Cr/Dr if negative
                }

                // Column 4: Value[Index 0] with Cr/Dr reversal if negative
                row.createCell(3).setCellValue(crDr);

                // Update the corresponding data in the "Unique Accounts" sheet
                updateUniqueAccountsSheet(workbook, key, crDr, valueAtIndex1);
            }

            // Write the data to the same Excel file
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void updateUniqueAccountsSheet(Workbook workbook, String key, String crDr, double updatedAmount) {
        Sheet sheet = workbook.getSheet("Unique Accounts");
        if (sheet == null) {
            System.out.println("Unique Accounts sheet not found.");
            return;
        }

        for (Row row : sheet) {
            Cell accountCell = row.getCell(0); // Assuming the account name is in column 0
            if (accountCell != null && key.equals(accountCell.getStringCellValue())) {
                // Update Account Type (Dr/Cr)
                Cell accountTypeCell = row.getCell(1); // Assuming Account Type is in column 1
                if (accountTypeCell != null) {
                    accountTypeCell.setCellValue(crDr);
                }

                // Update Current Balance
                Cell balanceCell = row.getCell(2); // Assuming Current Balance is in column 2
                if (balanceCell != null) {
                    balanceCell.setCellValue(updatedAmount);
                }
                break; // Exit after updating the relevant row
            }
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
