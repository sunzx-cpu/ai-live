package com.yaozhi.live.modules.biz.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.List;
import java.util.function.Consumer;

/**
 * Excel工具类
 *
 * @author renren
 * @email renren@renren.io
 * @date 2024-01-23 15:23:23
 */
public class ExcelUtils {

    /**
     * 获取单元格字符串值
     */
    public static String getCellStringValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                // 避免科学计数法显示
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    /**
     * 获取单元格整数值
     */
    public static Integer getCellIntValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        
        try {
            switch (cell.getCellType()) {
                case NUMERIC:
                    return (int) cell.getNumericCellValue();
                case STRING:
                    String str = cell.getStringCellValue().trim();
                    return str.isEmpty() ? null : Integer.parseInt(str);
                default:
                    return null;
            }
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 获取单元格Double值
     */
    public static Double getCellDoubleValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        
        try {
            switch (cell.getCellType()) {
                case NUMERIC:
                    return cell.getNumericCellValue();
                case STRING:
                    String str = cell.getStringCellValue().trim();
                    return str.isEmpty() ? null : Double.parseDouble(str);
                default:
                    return null;
            }
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 获取单元格Boolean值
     */
    public static Boolean getCellBooleanValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        
        try {
            switch (cell.getCellType()) {
                case BOOLEAN:
                    return cell.getBooleanCellValue();
                case STRING:
                    String str = cell.getStringCellValue().trim().toLowerCase();
                    return "true".equals(str) || "1".equals(str) || "是".equals(str);
                case NUMERIC:
                    return cell.getNumericCellValue() > 0;
                default:
                    return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 创建Excel工作簿
     */
    public static Workbook createWorkbook() {
        return new XSSFWorkbook();
    }

    /**
     * 创建标题样式
     */
    public static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerStyle.setFont(headerFont);
        
        // 设置背景色
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        // 设置边框
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        
        // 设置对齐方式
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        
        return headerStyle;
    }

    /**
     * 创建数据样式
     */
    public static CellStyle createDataStyle(Workbook workbook) {
        CellStyle dataStyle = workbook.createCellStyle();
        
        // 设置边框
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);
        dataStyle.setBorderTop(BorderStyle.THIN);
        
        // 设置对齐方式
        dataStyle.setAlignment(HorizontalAlignment.LEFT);
        dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        
        return dataStyle;
    }

    /**
     * 创建标题行
     */
    public static Row createHeaderRow(Sheet sheet, String[] headers) {
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }
        return headerRow;
    }

    /**
     * 自动调整列宽
     */
    public static void autoSizeColumns(Sheet sheet, int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);
            // 设置最小宽度
            int currentWidth = sheet.getColumnWidth(i);
            if (currentWidth < 2000) {
                sheet.setColumnWidth(i, 2000);
            }
            // 设置最大宽度
            if (currentWidth > 10000) {
                sheet.setColumnWidth(i, 10000);
            }
        }
    }

    /**
     * 输出Excel到响应流
     */
    public static void writeToResponse(Workbook workbook, HttpServletResponse response, String fileName) throws IOException {
        try {
            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("UTF-8");

            // 设置文件名，支持中文
            String encodedFileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);

            // 写入响应流
            OutputStream outputStream = response.getOutputStream();
            workbook.write(outputStream);
            outputStream.flush();
            workbook.close();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("Excel文件写入响应流失败: " + e.getMessage(), e);
        }
    }

    /**
     * 导出数据到Excel
     */
    public static <T> void exportToExcel(List<T> dataList, String[] headers, String sheetName, 
                                       HttpServletResponse response, String fileName,
                                       DataRowWriter<T> rowWriter) throws IOException {
        // 创建工作簿
        Workbook workbook = createWorkbook();
        Sheet sheet = workbook.createSheet(sheetName);
        
        // 创建样式
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        
        // 创建标题行
        Row headerRow = createHeaderRow(sheet, headers);
        for (int i = 0; i < headers.length; i++) {
            headerRow.getCell(i).setCellStyle(headerStyle);
        }
        
        // 写入数据
        for (int i = 0; i < dataList.size(); i++) {
            Row row = sheet.createRow(i + 1);
            T data = dataList.get(i);
            rowWriter.writeRow(row, data, dataStyle);
        }
        
        // 自动调整列宽
        autoSizeColumns(sheet, headers.length);
        
        // 输出到响应流
        writeToResponse(workbook, response, fileName);
    }

    /**
     * 数据行写入器接口
     */
    @FunctionalInterface
    public interface DataRowWriter<T> {
        void writeRow(Row row, T data, CellStyle cellStyle);
    }

    /**
     * 创建单元格并设置值和样式
     */
    public static Cell createCell(Row row, int columnIndex, String value, CellStyle cellStyle) {
        Cell cell = row.createCell(columnIndex);
        cell.setCellValue(value != null ? value : "");
        if (cellStyle != null) {
            cell.setCellStyle(cellStyle);
        }
        return cell;
    }

    /**
     * 创建单元格并设置数字值和样式
     */
    public static Cell createCell(Row row, int columnIndex, Number value, CellStyle cellStyle) {
        Cell cell = row.createCell(columnIndex);
        if (value != null) {
            cell.setCellValue(value.doubleValue());
        }
        if (cellStyle != null) {
            cell.setCellStyle(cellStyle);
        }
        return cell;
    }

    /**
     * 创建单元格并设置布尔值和样式
     */
    public static Cell createCell(Row row, int columnIndex, Boolean value, CellStyle cellStyle) {
        Cell cell = row.createCell(columnIndex);
        if (value != null) {
            cell.setCellValue(value);
        }
        if (cellStyle != null) {
            cell.setCellStyle(cellStyle);
        }
        return cell;
    }

    /**
     * 验证Excel文件格式
     */
    public static boolean isValidExcelFile(String fileName) {
        return fileName != null && (fileName.endsWith(".xlsx") || fileName.endsWith(".xls"));
    }

    /**
     * 检查行是否为空
     */
    public static boolean isEmptyRow(Row row) {
        if (row == null) {
            return true;
        }
        
        for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String cellValue = getCellStringValue(cell);
                if (cellValue != null && !cellValue.trim().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }
} 