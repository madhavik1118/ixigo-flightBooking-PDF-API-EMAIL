package setup;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;

/**
 * One-time setup utility to create the APITestData sheet in Ixigo.xlsx.
 * Run this main method once to set up the test data required for API tests.
 * 
 * Sheet Structure:
 * | TestCase         | Name       | Job             |
 * | CreateAndGetUser | Dhanunjaya | QA Engineer     |
 * | InvalidPostRequest |          |                 |
 */
public class ApiTestDataSetup {

    public static void main(String[] args) {
        String filePath = System.getProperty("user.dir") + "/src/test/resources/testData/Ixigo.xlsx";
        String sheetName = "APITestData";

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = WorkbookFactory.create(fis)) {

            // Remove existing sheet if present (to recreate fresh)
            int sheetIndex = workbook.getSheetIndex(sheetName);
            if (sheetIndex != -1) {
                workbook.removeSheetAt(sheetIndex);
            }

            // Create new sheet
            Sheet sheet = workbook.createSheet(sheetName);

            // Create header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("TestCase");
            headerRow.createCell(1).setCellValue("Name");
            headerRow.createCell(2).setCellValue("Job");
            headerRow.createCell(3).setCellValue("InvalidUserId");

            // Test data row 1: Valid POST + GET scenario
            Row row1 = sheet.createRow(1);
            row1.createCell(0).setCellValue("CreateAndGetUser");
            row1.createCell(1).setCellValue("Dhanunjaya");
            row1.createCell(2).setCellValue("QA Engineer");
            row1.createCell(3).setCellValue("");

            // Test data row 2: Invalid POST scenario
            Row row2 = sheet.createRow(2);
            row2.createCell(0).setCellValue("EmptyPostRequest");
            row2.createCell(1).setCellValue("");
            row2.createCell(2).setCellValue("");
            row2.createCell(3).setCellValue("");

            // Test data row 3: GET non-existent user (404 scenario)
            Row row3 = sheet.createRow(3);
            row3.createCell(0).setCellValue("GetNonExistentUser");
            row3.createCell(1).setCellValue("");
            row3.createCell(2).setCellValue("");
            row3.createCell(3).setCellValue("9999");

            // Auto-size columns
            for (int i = 0; i < 4; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write to file
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }

            System.out.println("APITestData sheet created successfully in: " + filePath);
            System.out.println("Test data rows added:");
            System.out.println("  1. CreateAndGetUser - Name: Dhanunjaya, Job: QA Engineer");
            System.out.println("  2. EmptyPostRequest - Empty payload test");
            System.out.println("  3. GetNonExistentUser - InvalidUserId: 9999");

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to set up API test data: " + e.getMessage());
        }
    }
}
