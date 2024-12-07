package org.example;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class Dummy {

    public static void main(String[] args) {
        String filePath = "C:\\Users\\VYAPAR APP\\IdeaProjects\\deskop_automation\\src\\test\\resources\\JournalEntries\\JE - TestData.xlsx";
        List<Map.Entry<String, List<String>>> keyValuePairs = readExcelData(filePath);

        // Display the key-value pairs
        keyValuePairs.forEach(entry -> {
            System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
        });
    }

    public static List<Map.Entry<String, List<String>>> readExcelData(String filePath) {
        List<Map.Entry<String, List<String>>> keyValueList = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            // Get the sheet named "Trial Balance report"
            Sheet sheet = workbook.getSheet("Total Balances");
            if (sheet == null) {
                throw new IllegalArgumentException("Sheet 'Trial Balance report' not found in the Excel file.");
            }

            // Iterate through rows
            for (Row row : sheet) {
                // Get the key from the first column
                Cell keyCell = row.getCell(0); // Column 1 is index 0
                if (keyCell != null && keyCell.getCellType() == CellType.STRING) {
                    String key = keyCell.getStringCellValue();

                        List<String> values = new ArrayList<>();

                        // Collect the rest of the column values
                        for (int i = 1; i < row.getLastCellNum(); i++) {
                            Cell valueCell = row.getCell(i);
                            if (valueCell != null) {
                                switch (valueCell.getCellType()) {
                                    case STRING:
                                        values.add(valueCell.getStringCellValue());
                                        break;
                                    case NUMERIC:
                                        values.add(String.valueOf(valueCell.getNumericCellValue()));
                                        break;
                                    case BOOLEAN:
                                        values.add(String.valueOf(valueCell.getBooleanCellValue()));
                                        break;
                                    default:
                                        values.add(""); // For blank cells
                                }
                            } else {
                                values.add(""); // Handle empty cells
                            }
                        }

                        // Add the key-value pair to the list
                        keyValueList.add(new AbstractMap.SimpleEntry<>(key, values));

                }
            }
        } catch (IOException e) {
            System.err.println("Error reading Excel file: " + e.getMessage());
        }

        return keyValueList;
    }
}

