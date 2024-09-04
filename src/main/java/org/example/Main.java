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

        System.out.println("Enter JE Expected Data Creation: Automatic or Manual ?");
        String creationType = scanner.nextLine();
        int crCount = 0;
        int drCount = 0;
        String adjustmentType = null;
        String transactionType = null;

        if (Objects.equals(creationType, "Automatic")) {
            System.out.println("Enter kind of JE to Execute: Adjustments or Transaction?");
            transactionType = scanner.nextLine();
            if (Objects.equals(transactionType, "Adjustments")) {
                System.out.println("Enter the Adjustment Type: Cr or Dr");
                adjustmentType = scanner.nextLine();
                if (Objects.equals(adjustmentType, "Cr")) {
                    System.out.println("Enter No. of Cr type Accounts to be Selected: ");
                    crCount = scanner.nextInt();
                } else if (Objects.equals(adjustmentType, "Dr")) {
                    System.out.println("Enter No. of Dr Accounts to be Selected?");
                    drCount = scanner.nextInt();
                } else {
                    System.out.println("Adjustment Type doesn't exists");
                }

            } else if (Objects.equals(transactionType, "Transaction")) {
                System.out.println("Enter No. of Cr type Accounts to be Selected: ");
                crCount = scanner.nextInt();

                System.out.println("Enter No. of Dr Accounts to be Selected?");
                drCount = scanner.nextInt();
            } else {
                System.out.println("Transaction Type doesn't exists");
            }


            List<Map.Entry<String, List<String>>> selectedEntries = pickAndPrintRandomEntries(dataList, crCount, drCount, adjustmentType, transactionType);

            System.out.print("Enter an amount: ");
            double amount = scanner.nextDouble();

            dataforJEUI(excelFilePath, "JE-UI", selectedEntries, amount);
            COAImpact.calculateAndCreateImpactSheet(excelFilePath, selectedEntries, amount);

        }


        // Pick and print random entries based on the input with equal Cr and Dr distribution


        // Populate the new sheet with the selected entries


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

    public static List<Map.Entry<String, List<String>>> pickAndPrintRandomEntries(List<Map.Entry<String, List<String>>> dataList, int crCount, int drCount, String adjustmentType,String transactionType) {
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

        // Shuffle both lists to ensure randomness
        Collections.shuffle(crEntries);
        Collections.shuffle(drEntries);

        // Pick the required number of entries from each list
        List<Map.Entry<String, List<String>>> selectedEntries = new ArrayList<>();

        if (transactionType =="Adjustment"){
            if(adjustmentType=="Cr"){
                selectedEntries.addAll(crEntries.subList(0, crCount));
            }else {
                selectedEntries.addAll(drEntries.subList(0, drCount));
            }
        }else{
            selectedEntries.addAll(crEntries.subList(0, crCount));
            selectedEntries.addAll(drEntries.subList(0, drCount));
        }


        return selectedEntries;
    }


}
