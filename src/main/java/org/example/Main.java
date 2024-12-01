package org.example;


import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.example.AutomaticJETestDataCreator.generateKeyValuePairs;
import static org.example.JEUI.dataforJEUI;


public class Main {

    public static  void main(String [] args) {
        String excelFilePath = "C:\\Users\\VYAPAR APP\\IdeaProjects\\Journal-Entries-Expected-Data-Creator\\src\\main\\resources\\JE - TestData.xlsx";
        String AllAccountssheetName = "Unique Accounts";

        // Read the key-value pairs from the Excel file and store them in a list
        List<Map.Entry<String, List<String>>> dataList = new ArrayList<>(readExcelFile(excelFilePath, "Unique Accounts").entrySet());
//        List<Map.Entry<String, List<String>>> AllAccountdataList = new ArrayList<>(readExcelFile(excelFilePath, AllAccountssheetName).entrySet());

        List<Map.Entry<String, List<String>>> updatedSelectedEntries = null;
//        List<Map.Entry<String, List<String>>> AllAccountSupdatedSelectedEntries = null;

        List<Map.Entry<String, List<String>>> selectedEntries = pickAndPrintRandomEntries(dataList, 20, 20, null, "Transaction");

        List<Map.Entry<String, List<String>>> jeEntries = generateKeyValuePairs(selectedEntries);

        if (checkEqualAmounts(jeEntries)) {
            dataforJEUI(excelFilePath, jeEntries);
            updatedSelectedEntries = JEImpactOnCOA.calculateAndCreateImpactSheet(excelFilePath, dataList, jeEntries);
//                AllAccountSupdatedSelectedEntries = JEImpactOnCOA.calculateAndCreateImpactSheet(excelFilePath, AllAccountdataList, jeEntries);

            // Update the Excel file with updated selected entries
            updateExcelFile(excelFilePath,AllAccountssheetName, updatedSelectedEntries);
            updateExcelFile(excelFilePath,"Unique Accounts", updatedSelectedEntries);

        } else {
            System.out.println("Both are not Equal");
        }


    }


    public static Map<String, List<String>> readExcelFile(String filePath, String sheetName) {
        Map<String, List<String>> dataMap = new LinkedHashMap<>();

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                throw new IllegalArgumentException("Sheet with name " + sheetName + " does not exist");
            }

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header row if it exists

                String key = getCellValue(row.getCell(0));
                List<String> values = new ArrayList<>();

                for (int i = 1; i < row.getLastCellNum(); i++) {
                    String cellValue = getCellValue(row.getCell(i));
                    if (i == row.getLastCellNum() - 1) {
                        // Split the last cell's value by comma and add each part as separate entries
                        values.addAll(Arrays.asList(cellValue.split(",")));
                    } else {
                        values.add(cellValue);
                    }
                }

                dataMap.put(key, values);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return dataMap;
    }

    public static String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    // Preserve decimals by returning the cell's numeric value as a string
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }


    public static List<Map.Entry<String, List<String>>> pickAndPrintRandomEntries(
            List<Map.Entry<String, List<String>>> dataList, int crCount, int drCount, String adjustmentType, String transactionType) {
        // List of accounts to exclude
        Set<String> excludedAccounts = new HashSet<>(Arrays.asList("CarLoan", "Krishna", "Fixed Car Asset", "Bank Assets"));

        // Separate entries into those with "Cr" and those with "Dr", excluding the unwanted accounts
        List<Map.Entry<String, List<String>>> crEntries = new ArrayList<>();
        List<Map.Entry<String, List<String>>> drEntries = new ArrayList<>();

        for (Map.Entry<String, List<String>> entry : dataList) {
            String key = entry.getKey();

            // Skip entries that belong to the excluded accounts
            if (excludedAccounts.contains(key)) {
                continue;
            }

            List<String> values = entry.getValue();
            for (String value : values) {
                if (value.contains("Cr")) {
                    crEntries.add(entry);
                    break;
                } else if (value.contains("Dr")) {
                    drEntries.add(entry);
                    break;
                }
            }
        }

        // Shuffle both lists to ensure randomness
        Collections.shuffle(crEntries);
        Collections.shuffle(drEntries);

        // Pick the required number of entries from each list
        List<Map.Entry<String, List<String>>> selectedEntries = new ArrayList<>();

        if ("Adjustment".equals(transactionType)) {
            if ("Cr".equals(adjustmentType)) {
                selectedEntries.addAll(crEntries.subList(0, Math.min(crCount, crEntries.size())));
            } else {
                selectedEntries.addAll(drEntries.subList(0, Math.min(drCount, drEntries.size())));
            }
        } else {
            selectedEntries.addAll(crEntries.subList(0, Math.min(crCount, crEntries.size())));
            selectedEntries.addAll(drEntries.subList(0, Math.min(drCount, drEntries.size())));
        }

        // Trim values for each selected entry
        List<Map.Entry<String, List<String>>> trimmedEntries = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : selectedEntries) {
            // Filter out empty values
            List<String> trimmedValues = entry.getValue().stream()
                    .filter(value -> !value.isEmpty())  // Remove empty strings
                    .collect(Collectors.toList());

            // Create a new entry with trimmed values
            trimmedEntries.add(new AbstractMap.SimpleEntry<>(entry.getKey(), trimmedValues));
        }

        return trimmedEntries;
    }


    // Function to check if the sums of 'Cr' and 'Dr' amounts are equal
    public static boolean checkEqualAmounts(List<Map.Entry<String, List<String>>> jeEntries) {
        double crSum = 0.0;
        double drSum = 0.0;

        for (Map.Entry<String, List<String>> entry : jeEntries) {
            // Parse the amount at the zero index
            double amount = Double.parseDouble(entry.getValue().get(0));
            // Get the amount type at the first index
            String amountType = entry.getValue().get(1);

            // Add to 'Cr' or 'Dr' sum based on amount type
            if ("Cr".equalsIgnoreCase(amountType)) {
                crSum += amount;
            } else if ("Dr".equalsIgnoreCase(amountType)) {
                drSum += amount;
            }
        }

        // Check if the sums are equal
        return crSum == drSum;
    }

    // Function to update the Excel file with updated selected entries
    public static void updateExcelFile(String filePath, String sheetName, List<Map.Entry<String, List<String>>> updatedEntries) {
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                throw new IllegalArgumentException("Sheet with name " + sheetName + " does not exist");
            }

            // Create a map for easy access of updated entries
            Map<String, List<String>> updatedEntriesMap = updatedEntries.stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            // Update rows in the sheet
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header row

                Cell keyCell = row.getCell(0);
                String key = getCellValue(keyCell);

                if (updatedEntriesMap.containsKey(key)) {
                    List<String> updatedValues = updatedEntriesMap.get(key);

                    // Update the cells with the new values
                    for (int i = 1; i <= updatedValues.size(); i++) {
                        Cell cell = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        cell.setCellValue(updatedValues.get(i - 1));
                    }
                }
            }

            // Write the changes back to the Excel file
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
