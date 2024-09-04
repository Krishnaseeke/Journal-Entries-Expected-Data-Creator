package org.example;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import static org.example.JEUI.dataforJEUI;


public class Main {

    public static void main(String[] args) {
        String excelFilePath = "C:\\Users\\E14-3\\Downloads\\JE - TestData.xlsx";
        String sheetName = "Unique Accounts";

        // Read the key-value pairs from the Excel file and store them in a list
        List<Map.Entry<String, List<String>>> dataList = new ArrayList<>(readExcelFile(excelFilePath, sheetName).entrySet());

//        // Print the key-value pairs
//        for (Map.Entry<String, List<String>> entry : dataList) {
//            System.out.println("Key: " + entry.getKey());
//            System.out.println("Values: " + entry.getValue());
//            System.out.println();
//        }

        // Get user input
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter an integer value: ");
        int input = scanner.nextInt();

        System.out.print("Enter an amount: ");
        double amount = scanner.nextDouble();

        // Pick and print random entries based on the input with equal Cr and Dr distribution
        List<Map.Entry<String, List<String>>> selectedEntries = pickAndPrintRandomEntries(dataList, input);

        // Populate the new sheet with the selected entries
        dataforJEUI(excelFilePath, "JE-UI", selectedEntries, amount);
        COAImpact.calculateAndCreateImpactSheet(excelFilePath, selectedEntries, amount);

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
                    values.add(getCellValue(row.getCell(i)));
                }

                // Process the last value to handle multiple entries separated by a comma
                if (!values.isEmpty()) {
                    String lastValue = values.get(values.size() - 1);
                    List<String> arrayValues = Arrays.asList(lastValue.split(","));
                    values.set(values.size() - 1, String.join(", ", arrayValues));
                }

                dataMap.put(key, values);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return dataMap;
    }

    private static String getCellValue(Cell cell) {
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
                    return String.valueOf((int) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    public static List<Map.Entry<String, List<String>>> pickAndPrintRandomEntries(List<Map.Entry<String, List<String>>> dataList, int input) {
        int numberOfEntriesToPick = input * 2;

        // Separate entries into those with "Cr" and those with "Dr"
        List<Map.Entry<String, List<String>>> crEntries = new ArrayList<>();
        List<Map.Entry<String, List<String>>> drEntries = new ArrayList<>();

        for (Map.Entry<String, List<String>> entry : dataList) {
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

        // Determine the number of entries to pick from each list
        int halfToPick = numberOfEntriesToPick / 2;

        // Shuffle both lists to ensure randomness
        Collections.shuffle(crEntries);
        Collections.shuffle(drEntries);

        // Ensure we don't pick more entries than available
        if (halfToPick > crEntries.size()) {
            halfToPick = crEntries.size();
        }
        if (halfToPick > drEntries.size()) {
            halfToPick = drEntries.size();
        }

        // Pick the required number of entries from each list
        List<Map.Entry<String, List<String>>> selectedEntries = new ArrayList<>();
        selectedEntries.addAll(crEntries.subList(0, halfToPick));
        selectedEntries.addAll(drEntries.subList(0, halfToPick));

        // Print the selected entries
//        for (Map.Entry<String, List<String>> entry : selectedEntries) {
//            System.out.println("Picked Key: " + entry.getKey());
//            System.out.println("Picked Values: " + entry.getValue());
//            System.out.println();
//        }

        return selectedEntries;
    }


}
