package org.example;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.example.JEUI.dataforJEUI;


public class Main {


    public static void main(String[] args) {
        String excelFilePath = "C:\\Users\\E14-3\\Downloads\\JE - TestData.xlsx";
        String sheetName = "Unique Accounts";

        // Read the key-value pairs from the Excel file and store them in a list
        List<Map.Entry<String, List<String>>> dataList = new ArrayList<>(readExcelFile(excelFilePath, sheetName).entrySet());

        for ( Map.Entry<String, List<String>> data: dataList){
            System.out.println(data.getKey()+"------"+data.getValue());
        }

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
                    System.out.println("Adjustment Type doesn't exist");
                    return;
                }

            } else if (Objects.equals(transactionType, "Transaction")) {
                System.out.println("Enter No. of Cr type Accounts to be Selected: ");
                crCount = scanner.nextInt();

                System.out.println("Enter No. of Dr Accounts to be Selected?");
                drCount = scanner.nextInt();
            } else {
                System.out.println("Transaction Type doesn't exist");
                return;
            }

            List<Map.Entry<String, List<String>>> selectedEntries = pickAndPrintRandomEntries(dataList, crCount, drCount, adjustmentType, transactionType);

            double amount = 0.0;
            String amountType;

            List<Map.Entry<String, List<String>>> jeEntries = new ArrayList<>(); // Initialize the list

            for (Map.Entry<String, List<String>> entry : selectedEntries) {
                System.out.print("Enter an amount for key " + entry.getKey() + ": ");
                amount = scanner.nextDouble();

                // Update the amount to the entry value at zero index
                entry.getValue().set(0, String.valueOf(amount));

                // Consume the remaining newline left by nextDouble()
                scanner.nextLine();

                // Print the current amount type at index 1
                System.out.print("Enter the Amount Type (Cr or Dr) for key " + entry.getKey() + " (Current amountType: " + entry.getValue().get(1) + "): ");
                amountType = scanner.nextLine();

                // Update the amount type to the entry value at first index
                entry.getValue().set(1, amountType);

                // Add the updated entry to the list
                jeEntries.add(entry);
            }

            if (checkEqualAmounts(jeEntries)) {
                dataforJEUI(excelFilePath,jeEntries);
                COAImpact.calculateAndCreateImpactSheet(excelFilePath,dataList , jeEntries);
            } else {
                System.out.println("Both are not Equal");
            }


            // Populate data into 'JE-UI' and 'Impact' sheets

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

}
