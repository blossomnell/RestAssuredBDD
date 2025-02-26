package utilities;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;

public class ExcelReader {

    private Workbook workbook;

    public ExcelReader(String filePath) throws IOException {
        FileInputStream fis = new FileInputStream(filePath);
        workbook = new XSSFWorkbook(fis);
    }

    public String getCellData(String sheetName, int rowNum, int colNum) {
        try {
            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                throw new RuntimeException("Sheet not found: " + sheetName);
            }

            // Adjusting for zero-based index
            Row row = sheet.getRow(rowNum - 1); 
            if (row == null) {
                System.out.println("Warning: Row " + rowNum + " is empty!");
                return "";
            }

            Cell cell = row.getCell(colNum);
            if (cell == null || cell.getCellType() == CellType.BLANK) {
                System.out.println("Warning: Empty cell at Row " + rowNum + ", Column " + colNum);
                return ""; // Return empty string if cell is null or blank
            }

            // Debug: Print cell value before returning
            System.out.print("Reading Cell Data (Row: " + rowNum + ", Col: " + colNum + "): ");

            // Handle different cell types
            String cellValue;
            switch (cell.getCellType()) {
                case STRING:
                    cellValue = cell.getStringCellValue().trim(); // Remove leading/trailing spaces
                    break;
                case NUMERIC:
                    cellValue = String.valueOf((int) cell.getNumericCellValue()); // Convert numeric to int if needed
                    break;
                case BOOLEAN:
                    cellValue = String.valueOf(cell.getBooleanCellValue());
                    break;
                default:
                    cellValue = "";
            }

            System.out.println("[" + cellValue + "]"); // Print the value
            return cellValue;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public int getRowCount(String sheetName) {
        Sheet sheet = workbook.getSheet(sheetName);
        return sheet.getLastRowNum() + 1;
    }

    public int getColumnCount(String sheetName, int rowIndex) {
        Row row = workbook.getSheet(sheetName).getRow(rowIndex - 1); // Adjusting for zero-based index
        return (row != null) ? row.getLastCellNum() : 0;
    }

    public void closeWorkbook() throws IOException {
        workbook.close();
    }
}
