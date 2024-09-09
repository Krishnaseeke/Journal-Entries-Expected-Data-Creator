package org.example;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.awt.Color;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

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

            // Create a header row
            Row headerRow = impactSheet.createRow(0);
            headerRow.createCell(0).setCellValue("Account Name");
            headerRow.createCell(1).setCellValue("Current Balance");
            headerRow.createCell(2).setCellValue("Amount Type");

            // Define a CellStyle for the sky-blue background
            CellStyle skyBlueStyle = workbook.createCellStyle();
            XSSFColor skyBlue = new XSSFColor(new Color(135, 206, 235), null); // RGB for sky-blue
            ((XSSFCellStyle) skyBlueStyle).setFillForegroundColor(skyBlue);
            skyBlueStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Process and update entries based on JE entries
            Map<String, List<String>> originalValues = new HashMap<>();
            for (Map.Entry<String, List<String>> entry : selectedEntries) {
                originalValues.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }

            // Update selected entries based on JE entries
            updateSelectedEntries(selectedEntries, jeEntries);


            // Group entries by SelectedEntries.value[2]
            Map<String, List<Map.Entry<String, List<String>>>> groupedEntries = groupEntriesByCategory(selectedEntries);

            // Populate the "Impact" sheet with grouped and updated selectedEntries
            int rowIndex = 1; // Start after header
// Create a light green style for group headers
            CellStyle lightGreenStyle = impactSheet.getWorkbook().createCellStyle();
            lightGreenStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            lightGreenStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            for (Map.Entry<String, List<Map.Entry<String, List<String>>>> group : groupedEntries.entrySet()) {
                // Insert a header row for each group
                Row groupHeaderRow = impactSheet.createRow(rowIndex++);
                Cell groupHeaderCell = groupHeaderRow.createCell(0);
                groupHeaderCell.setCellValue(group.getKey());

                // Apply the light green style to the group header
                groupHeaderCell.setCellStyle(lightGreenStyle);

                // Add the entries under the group
                for (Map.Entry<String, List<String>> entry : group.getValue()) {
                    Row row = impactSheet.createRow(rowIndex++);
                    List<String> values = entry.getValue();

                    Cell accountNameCell = row.createCell(0); // Column 1: Account Name
                    accountNameCell.setCellValue(entry.getKey());

                    Cell balanceCell = row.createCell(1); // Column 2: Current Balance
                    balanceCell.setCellValue(parseDoubleOrZero(values.get(0)));

                    Cell amountTypeCell = row.createCell(2); // Column 3: Amount Type (Cr/Dr)
                    amountTypeCell.setCellValue(values.get(1));

                    // Apply sky-blue style if the value has been updated
                    if (originalValues.containsKey(entry.getKey())) {
                        List<String> originalValue = originalValues.get(entry.getKey());
                        double originalAmount = parseDoubleOrZero(originalValue.get(0));
                        double updatedAmount = parseDoubleOrZero(values.get(0));
                        if (originalAmount != updatedAmount) {
                            accountNameCell.setCellStyle(skyBlueStyle);
                            balanceCell.setCellStyle(skyBlueStyle);
                            amountTypeCell.setCellStyle(skyBlueStyle);
                        }
                    }
                }
            }


            // Write back to the same Excel file
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        TotalBalances.summarizeGroups(filePath,selectedEntries);
    }

    // Method to update selected entries based on JE entries
    private static void updateSelectedEntries(List<Map.Entry<String, List<String>>> selectedEntries, List<Map.Entry<String, List<String>>> jeEntries) {
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
    }

    // Method to group entries by SelectedEntries.value[2]
    private static Map<String, List<Map.Entry<String, List<String>>>> groupEntriesByCategory(List<Map.Entry<String, List<String>>> selectedEntries) {
        Map<String, List<Map.Entry<String, List<String>>>> groupedEntries = new LinkedHashMap<>();

        for (Map.Entry<String, List<String>> entry : selectedEntries) {

            // Check for null or empty value list
            if (entry.getValue() == null || entry.getValue().isEmpty()) {
                System.out.println("Skipping entry with empty or null value list: " + entry.getKey());
                continue; // Skip to the next entry
            }

            // Check the size of the list again before accessing elements
            if (entry.getValue().size() >= 3) {
                String groupKey = entry.getValue().get(2); // Use value[2] as the group key

                groupedEntries.putIfAbsent(groupKey, new ArrayList<>());
                groupedEntries.get(groupKey).add(entry);
            } else {
                System.out.println("Skipping entry due to insufficient values: " + entry.getKey());
            }
        }

        return groupedEntries;
    }


    // Utility method to parse a double value or return zero if parsing fails
    private static double parseDoubleOrZero(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0; // Return 0 if the value cannot be parsed as a double
        }
    }
}
