package org.example;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class TotalBalances {

    public static void summarizeGroups(String filePath, List<Map.Entry<String, List<String>>> selectedEntries) {
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            // Create or clear the "Total Balances" sheet
            Sheet totalBalancesSheet = workbook.getSheet("Total Balances");
            if (totalBalancesSheet == null) {
                totalBalancesSheet = workbook.createSheet("Total Balances");
            } else {
                // Clear existing content
                int lastRow = totalBalancesSheet.getLastRowNum();
                for (int i = 0; i <= lastRow; i++) {
                    Row row = totalBalancesSheet.getRow(i);
                    if (row != null) {
                        totalBalancesSheet.removeRow(row);
                    }
                }
            }

            // Header row
            Row headerRow = totalBalancesSheet.createRow(0);
            headerRow.createCell(0).setCellValue("Group Name");
            headerRow.createCell(1).setCellValue("Summation Value");

            // Group entries by selectedEntries.value[2]
            Map<String, List<Map.Entry<String, List<String>>>> groupedEntries = groupEntriesByCategory(selectedEntries);

            // Summarize each group
            Map<String, Double> groupSums = summarizeGroups(groupedEntries);

            // Populate the "Total Balances" sheet with group names and summation values
            int rowIndex = 1; // Start after header
            for (Map.Entry<String, Double> entry : groupSums.entrySet()) {
                Row row = totalBalancesSheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(entry.getKey()); // Column 1: Group Name
                row.createCell(1).setCellValue(entry.getValue()); // Column 2: Summation Value
            }

            // Write back to the same Excel file
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to group entries by selectedEntries.value[2]
    private static Map<String, List<Map.Entry<String, List<String>>>> groupEntriesByCategory(List<Map.Entry<String, List<String>>> selectedEntries) {
        Map<String, List<Map.Entry<String, List<String>>>> groupedEntries = new LinkedHashMap<>();

        for (Map.Entry<String, List<String>> entry : selectedEntries) {


            if (entry.getValue() == null || entry.getValue().isEmpty()) {
                System.out.println("Skipping entry with empty or null value list: " + entry.getKey());
                continue; // Skip to the next entry
            }

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

    // Method to summarize groups based on the specified logic
    private static Map<String, Double> summarizeGroups(Map<String, List<Map.Entry<String, List<String>>>> groupedEntries) {
        Map<String, Double> groupSums = new LinkedHashMap<>();

        for (Map.Entry<String, List<Map.Entry<String, List<String>>>> group : groupedEntries.entrySet()) {
            String groupName = group.getKey();
            List<Map.Entry<String, List<String>>> entries = group.getValue();

            double sum = 0;
            boolean isAssetsOrExpenses = groupName.equals("Assets") || groupName.equals("Expenses");
            boolean isEquitiesOrLiabilities = groupName.equals("Equities and Liabilities") || groupName.equals("Incomes");

            for (Map.Entry<String, List<String>> entry : entries) {
                List<String> values = entry.getValue();
                double amount = parseDoubleOrZero(values.get(0).trim());
                String crDr = values.get(1).trim();

                if (isAssetsOrExpenses) {
                    sum += crDr.equals("Dr") ? amount : -amount;
                } else if (isEquitiesOrLiabilities) {
                    sum += crDr.equals("Cr") ? amount : -amount;
                }
            }

            groupSums.put(groupName, sum);
        }

        return groupSums;
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
