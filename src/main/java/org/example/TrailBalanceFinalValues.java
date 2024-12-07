package org.example;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class TrailBalanceFinalValues {

    public static void main(String[] args) {
        String filePath = "C:\\Users\\VYAPAR APP\\IdeaProjects\\deskop_automation\\src\\test\\resources\\Chart_Of_Accounts_Backup\\Trial Balance Report_01_04_16_to_06_12_24.xlsx";
        List<Map.Entry<String, Double>> keyValuePairs = readExcelData(filePath);

        // Display the key-value pairs
        keyValuePairs.forEach(entry -> {
            System.out.println("Key: " + entry.getKey() + ", Absolute Difference: " + entry.getValue());
        });
    }

    public static List<Map.Entry<String, Double>> readExcelData(String filePath) {
        List<Map.Entry<String, Double>> keyValueList = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            // Get the sheet named "Trial Balance report"
            Sheet sheet = workbook.getSheet("Trial Balance report");
            if (sheet == null) {
                throw new IllegalArgumentException("Sheet 'Trial Balance report' not found in the Excel file.");
            }

            // Iterate through rows
            for (Row row : sheet) {
                // Get the key from the first column
                Cell keyCell = row.getCell(0); // Column 1 is index 0
                if (keyCell != null && keyCell.getCellType() == CellType.STRING) {
                    String key = keyCell.getStringCellValue();

                    // Check if the key matches "Total"
                    if ("Total".equalsIgnoreCase(key)) {
                        Cell value1Cell = row.getCell(1); // Value1 is in column 2
                        Cell value2Cell = row.getCell(2); // Value2 is in column 3


                        double value1 = 0.0;
                        double value2 = 0.0;

                        // Safely parse numeric values from the cells
                        if (value1Cell != null) {
                            if (value1Cell.getCellType() == CellType.NUMERIC) {
                                value1 = value1Cell.getNumericCellValue();
                            } else if (value1Cell.getCellType() == CellType.STRING) {
                                value1 = Double.parseDouble(value1Cell.getStringCellValue().trim());
                            }
                        }

                        if (value2Cell != null) {
                            if (value2Cell.getCellType() == CellType.NUMERIC) {
                                value2 = value2Cell.getNumericCellValue();
                            } else if (value2Cell.getCellType() == CellType.STRING) {
                                value2 = Double.parseDouble(value2Cell.getStringCellValue().trim());
                            }
                        }

                        // Compute the absolute difference
                        double absoluteDifference = Math.abs(value1 - value2);
                        keyValueList.add(new AbstractMap.SimpleEntry<>(key + "_" + keyValueList.size(), absoluteDifference));
                    }
                }
            }
        } catch (NumberFormatException e) {
            System.err.println("Error parsing numeric value: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Error reading Excel file: " + e.getMessage());
        }

        return keyValueList;
    }



}
