package org.example;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
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
            Map<String, BigDecimal> groupSums = summarizeGroups(groupedEntries);

            // Populate the "Total Balances" sheet with group names and summation values
            int rowIndex = 1; // Start after header
            for (Map.Entry<String, BigDecimal> entry : groupSums.entrySet()) {
                Row row = totalBalancesSheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(entry.getKey()); // Column 1: Group Name
                row.createCell(1).setCellValue(entry.getValue().doubleValue()); // Column 2: Summation Value as double
            }

            // Calculate Total PnL Balance
            BigDecimal incomes = groupSums.getOrDefault("Incomes", BigDecimal.ZERO);
            BigDecimal expenses = groupSums.getOrDefault("Expenses", BigDecimal.ZERO);
            BigDecimal openingStock = new BigDecimal("10576.00"); // Given opening stock value
            BigDecimal closingStock = new BigDecimal("176918.76"); // Given closing stock value
            BigDecimal totalPnL = incomes.subtract(expenses).subtract(openingStock).add(closingStock);

            // Header for Total PnL Balance
            Row pnlHeaderRow = totalBalancesSheet.createRow(rowIndex++);
            pnlHeaderRow.createCell(0).setCellValue("Total PnL Balance [Net Income Profit/Loss] = [Incomes-Expenses]-Opening Stock+Closing Stock");

            // PnL Value row
            Row pnlValueRow = totalBalancesSheet.createRow(rowIndex);
            pnlValueRow.createCell(0).setCellValue(totalPnL.compareTo(BigDecimal.ZERO) < 0 ? "Dr" : "Cr"); // Column 1: Dr/Cr
            pnlValueRow.createCell(1).setCellValue(totalPnL.abs().doubleValue()); // Column 2: Absolute value of PnL

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
    private static Map<String, BigDecimal> summarizeGroups(Map<String, List<Map.Entry<String, List<String>>>> groupedEntries) {
        Map<String, BigDecimal> groupSums = new LinkedHashMap<>();

        for (Map.Entry<String, List<Map.Entry<String, List<String>>>> group : groupedEntries.entrySet()) {
            String groupName = group.getKey();
            List<Map.Entry<String, List<String>>> entries = group.getValue();

            BigDecimal sum = BigDecimal.ZERO;
            boolean isAssetsOrExpenses = groupName.equals("Assets") || groupName.equals("Expenses");
            boolean isEquitiesOrLiabilities = groupName.equals("Equities and Liabilities") || groupName.equals("Incomes");

            for (Map.Entry<String, List<String>> entry : entries) {
                List<String> values = entry.getValue();
                BigDecimal amount = parseBigDecimalOrZero(values.get(0).trim());
                String crDr = values.get(1).trim();

                if (isAssetsOrExpenses) {
                    sum = sum.add(crDr.equals("Dr") ? amount : amount.negate());
                } else if (isEquitiesOrLiabilities) {
                    sum = sum.add(crDr.equals("Cr") ? amount : amount.negate());
                }
            }

            groupSums.put(groupName, sum);
        }

        return groupSums;
    }

    // Utility method to parse a BigDecimal value or return zero if parsing fails
    private static BigDecimal parseBigDecimalOrZero(String value) {
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO; // Return 0 if the value cannot be parsed as a BigDecimal
        }
    }
}
