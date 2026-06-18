package utils;

import org.apache.poi.ss.formula.functions.Column;
import org.apache.poi.ss.usermodel.*;
import org.openqa.selenium.WebDriver;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExcelUtil {
	static List<Map<String, String>> sheetData;

	/**
	 * Reads an entire Excel sheet and returns data as a List of Maps. Each Map
	 * represents a row, where Key = Column Header and Value = Cell Value.
	 */
	public static List<Map<String, String>> getSheetData(String filePath, String sheetName, String testCase) {
		sheetData = new ArrayList<>();

		try (FileInputStream fis = new FileInputStream(filePath); Workbook workbook = WorkbookFactory.create(fis)) {

			Sheet sheet = workbook.getSheet(sheetName);
			Row headerRow = sheet.getRow(0);
			int testCaseRow = 0;

			boolean found = false;
			for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
				Row row = sheet.getRow(i);
				DataFormatter formatter = new DataFormatter();
				String cellData = formatter.formatCellValue(row.getCell(0)).trim();

				if (cellData.equals(testCase)) {
					testCaseRow = i;
					found = true;
					break;
				}
			}
			if (!found) {
				throw new RuntimeException("Test case '" + testCase + "' not found in Excel sheet '" + sheetName + "'. Check the scenario name in column A.");
			}
			int totalCols = headerRow.getPhysicalNumberOfCells();

			Row currentRow = sheet.getRow(testCaseRow);

			Map<String, String> rowData = new HashMap<>();
			for (int j = 0; j < totalCols; j++) {
				String headerName = headerRow.getCell(j).getStringCellValue().trim();
				Cell cell = currentRow.getCell(j);

				// Format all cell types (String, Numeric, Boolean) into a String
				DataFormatter formatter = new DataFormatter();
				String cellValue = formatter.formatCellValue(cell).trim();

				rowData.put(headerName, cellValue);
			}
			sheetData.add(rowData);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to read Excel file at: " + filePath);
		}
		return sheetData;
	}

	public static List<Map<String, String>> getData() {
		return sheetData;

	}

	/**
	 * Retrieves the value of a specific column from a specific row index.
	 */
	public static String getCellValue(List<Map<String, String>> sheetData, String columnName) {
		if (0 < 0 || 0 >= sheetData.size()) {
			throw new IllegalArgumentException("Invalid row index: " + 0);
		}
		Map<String, String> rowData = sheetData.get(0);
		if (!rowData.containsKey(columnName)) {
			throw new IllegalArgumentException("Column '" + columnName + "' not found in the sheet data.");
		}
		return rowData.get(columnName);
	}
}
