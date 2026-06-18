package utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for reading API test data from and writing API response data to Excel.
 */
public class ApiExcelUtil {

    /**
     * Reads API test data from the specified sheet and test case row.
     * Expected columns: TestCase, Name, Job (for POST request body)
     */
    public static Map<String, String> getApiTestData(String filePath, String sheetName, String testCaseName) {
        Map<String, String> testData = new HashMap<>();

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                throw new RuntimeException("Sheet '" + sheetName + "' not found in: " + filePath);
            }

            Row headerRow = sheet.getRow(0);
            int totalCols = headerRow.getPhysicalNumberOfCells();
            int testCaseRowIndex = -1;

            // Find the row matching the test case name
            DataFormatter formatter = new DataFormatter();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    String cellValue = formatter.formatCellValue(row.getCell(0)).trim();
                    if (cellValue.equals(testCaseName)) {
                        testCaseRowIndex = i;
                        break;
                    }
                }
            }

            if (testCaseRowIndex == -1) {
                throw new RuntimeException("Test case '" + testCaseName + "' not found in sheet: " + sheetName);
            }

            Row dataRow = sheet.getRow(testCaseRowIndex);
            for (int j = 0; j < totalCols; j++) {
                String headerName = formatter.formatCellValue(headerRow.getCell(j)).trim();
                String cellValue = formatter.formatCellValue(dataRow.getCell(j)).trim();
                testData.put(headerName, cellValue);
            }

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to read API test data from: " + filePath);
        }

        return testData;
    }

    /**
     * Writes API response data back to the Excel sheet in the same test case row.
     * Creates columns if they don't exist: ResponseId, PostStatusCode, GetStatusCode, GetResponseBody
     */
    public static void writeApiResponseData(String filePath, String sheetName, String testCaseName,
                                            Map<String, String> responseData) {

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                throw new RuntimeException("Sheet '" + sheetName + "' not found in: " + filePath);
            }

            Row headerRow = sheet.getRow(0);
            int totalCols = headerRow.getPhysicalNumberOfCells();
            DataFormatter formatter = new DataFormatter();

            // Find the test case row
            int testCaseRowIndex = -1;
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    String cellValue = formatter.formatCellValue(row.getCell(0)).trim();
                    if (cellValue.equals(testCaseName)) {
                        testCaseRowIndex = i;
                        break;
                    }
                }
            }

            if (testCaseRowIndex == -1) {
                throw new RuntimeException("Test case '" + testCaseName + "' not found for writing response data.");
            }

            Row dataRow = sheet.getRow(testCaseRowIndex);

            // For each response data entry, find or create the column and write the value
            for (Map.Entry<String, String> entry : responseData.entrySet()) {
                String columnName = entry.getKey();
                String value = entry.getValue();

                int colIndex = findOrCreateColumn(headerRow, columnName, totalCols);
                if (colIndex >= totalCols) {
                    totalCols = colIndex + 1;
                }

                Cell cell = dataRow.getCell(colIndex);
                if (cell == null) {
                    cell = dataRow.createCell(colIndex);
                }
                cell.setCellValue(value);
            }

            // Write back to file
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to write API response data to: " + filePath);
        }
    }

    /**
     * Finds an existing column by header name or creates a new one.
     */
    private static int findOrCreateColumn(Row headerRow, String columnName, int currentTotalCols) {
        DataFormatter formatter = new DataFormatter();
        for (int i = 0; i < currentTotalCols; i++) {
            Cell cell = headerRow.getCell(i);
            if (cell != null && formatter.formatCellValue(cell).trim().equals(columnName)) {
                return i;
            }
        }
        // Column not found, create it
        Cell newHeaderCell = headerRow.createCell(currentTotalCols);
        newHeaderCell.setCellValue(columnName);
        return currentTotalCols;
    }

    /**
     * Creates the API test data sheet if it doesn't already exist.
     * Sets up headers: TestCase, Name, Job
     */
    public static void createApiSheetIfNotExists(String filePath, String sheetName) {
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                sheet = workbook.createSheet(sheetName);
                Row headerRow = sheet.createRow(0);
                headerRow.createCell(0).setCellValue("TestCase");
                headerRow.createCell(1).setCellValue("Name");
                headerRow.createCell(2).setCellValue("Job");

                try (FileOutputStream fos = new FileOutputStream(filePath)) {
                    workbook.write(fos);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
