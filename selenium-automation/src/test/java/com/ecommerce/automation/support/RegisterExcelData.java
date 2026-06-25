package com.ecommerce.automation.support;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public final class RegisterExcelData {
    private static final Path DEFAULT_FILE = Path.of("test-data", "register-test-data.xlsx");
    private static final String RUN_ID = String.valueOf(Instant.now().toEpochMilli());

    private RegisterExcelData() {
    }

    public static Object[][] rows(String sheetName) {
        List<RegisterTestData> testData = readSheet(sheetName);
        Object[][] data = new Object[testData.size()][1];
        for (int index = 0; index < testData.size(); index++) {
            data[index][0] = testData.get(index);
        }
        return data;
    }

    private static List<RegisterTestData> readSheet(String sheetName) {
        Path file = Path.of(System.getProperty("registerDataFile", DEFAULT_FILE.toString()));
        List<RegisterTestData> data = new ArrayList<>();

        try (InputStream inputStream = Files.newInputStream(file);
             Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                throw new IllegalArgumentException("Sheet not found in register Excel file: " + sheetName);
            }

            DataFormatter formatter = new DataFormatter();
            Iterator<Row> rows = sheet.rowIterator();
            if (rows.hasNext()) {
                rows.next();
            }

            while (rows.hasNext()) {
                Row row = rows.next();
                if (isBlankRow(row, formatter)) {
                    continue;
                }
                data.add(new RegisterTestData(
                        cell(row, 0, formatter),
                        cell(row, 1, formatter),
                        cell(row, 2, formatter),
                        cell(row, 3, formatter),
                        withRunId(cell(row, 4, formatter)),
                        cell(row, 5, formatter),
                        cell(row, 6, formatter),
                        cell(row, 7, formatter),
                        "yes".equalsIgnoreCase(cell(row, 8, formatter)),
                        cell(row, 9, formatter)
                ));
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read register Excel file: " + file.toAbsolutePath(), exception);
        }

        return data;
    }

    private static boolean isBlankRow(Row row, DataFormatter formatter) {
        for (int index = 0; index < 10; index++) {
            if (!cell(row, index, formatter).isBlank()) {
                return false;
            }
        }
        return true;
    }

    private static String cell(Row row, int index, DataFormatter formatter) {
        Cell cell = row.getCell(index, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        return cell == null ? "" : formatter.formatCellValue(cell).trim();
    }

    private static String withRunId(String value) {
        return value.replace("{RUN_ID}", RUN_ID);
    }
}
