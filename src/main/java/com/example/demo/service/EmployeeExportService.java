package com.example.demo.service;

import com.example.demo.dto.EmployeeDTO;
import com.example.demo.dto.EmployeeSearchCriteria;
import com.example.demo.mapper.EmployeeMapper;
import com.example.demo.model.Employee;
import com.example.demo.repository.EmployeeRepository;
import com.example.demo.repository.EmployeeSpecifications;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class EmployeeExportService {

    private static final String[] HEADERS = {
            "ID", "Name", "Email", "Department", "Type", "Grade", "Salary", "Joining Date", "Supervisor", "Has Login"
    };

    @Autowired private EmployeeRepository employeeRepository;

    private List<EmployeeDTO> fetch(EmployeeSearchCriteria criteria) {
        Specification<Employee> spec = EmployeeSpecifications.search(criteria);
        return employeeRepository.findAll(spec).stream().map(EmployeeMapper::toDto).toList();
    }

    private String[] rowValues(EmployeeDTO dto) {
        return new String[]{
                String.valueOf(dto.empId()),
                nullSafe(dto.name()),
                nullSafe(dto.email()),
                nullSafe(dto.departmentName()),
                dto.employeeType() != null ? dto.employeeType().name() : "",
                dto.grade() != null ? dto.grade().name() : "",
                dto.salary() != null ? String.valueOf(dto.salary()) : "",
                dto.joiningDate() != null ? dto.joiningDate().toString() : "",
                dto.supervisor() ? "Yes" : "No",
                dto.loginUsername() != null ? "Yes" : "No"
        };
    }

    private String nullSafe(String s) {
        return s == null ? "" : s;
    }

    public byte[] exportCsv(EmployeeSearchCriteria criteria) {
        List<EmployeeDTO> employees = fetch(criteria);
        StringBuilder sb = new StringBuilder(String.join(",", HEADERS)).append('\n');
        for (EmployeeDTO dto : employees) {
            String[] values = rowValues(dto);
            for (int i = 0; i < values.length; i++) {
                sb.append('"').append(values[i].replace("\"", "\"\"")).append('"');
                sb.append(i < values.length - 1 ? "," : "\n");
            }
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] exportExcel(EmployeeSearchCriteria criteria) throws IOException {
        List<EmployeeDTO> employees = fetch(criteria);
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("Employees");

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                headerRow.createCell(i).setCellValue(HEADERS[i]);
            }

            int rowIdx = 1;
            for (EmployeeDTO dto : employees) {
                Row row = sheet.createRow(rowIdx++);
                String[] values = rowValues(dto);
                for (int i = 0; i < values.length; i++) {
                    Cell cell = row.createCell(i);
                    cell.setCellValue(values[i]);
                }
            }

            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    public byte[] exportWord(EmployeeSearchCriteria criteria) throws IOException {
        List<EmployeeDTO> employees = fetch(criteria);
        try (XWPFDocument document = new XWPFDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            document.createParagraph().createRun().setText("Employee List");

            XWPFTable table = document.createTable(employees.size() + 1, HEADERS.length);
            XWPFTableRow headerRow = table.getRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                XWPFTableCell cell = headerRow.getCell(i);
                cell.setText(HEADERS[i]);
            }

            int rowIdx = 1;
            for (EmployeeDTO dto : employees) {
                XWPFTableRow row = table.getRow(rowIdx++);
                String[] values = rowValues(dto);
                for (int i = 0; i < values.length; i++) {
                    row.getCell(i).setText(values[i]);
                }
            }

            document.write(out);
            return out.toByteArray();
        }
    }

    public byte[] exportPdf(EmployeeSearchCriteria criteria) throws IOException {
        List<EmployeeDTO> employees = fetch(criteria);
        // Compact column set for PDF — full 10-column table doesn't fit a portrait page legibly
        String[] pdfHeaders = {"ID", "Name", "Email", "Department", "Salary"};
        float[] colWidths = {30, 100, 150, 90, 70};
        float margin = 40;
        float rowHeight = 18;
        float fontSize = 9;

        try (PDDocument document = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            PDPageContentStream content = new PDPageContentStream(document, page);
            float y = page.getMediaBox().getHeight() - margin;

            content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
            content.beginText();
            content.newLineAtOffset(margin, y);
            content.showText("Employee List");
            content.endText();
            y -= 30;

            content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), fontSize);
            y = drawRow(content, pdfHeaders, colWidths, margin, y, rowHeight);
            content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), fontSize);

            for (EmployeeDTO dto : employees) {
                if (y < margin + rowHeight) {
                    content.close();
                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);
                    content = new PDPageContentStream(document, page);
                    y = page.getMediaBox().getHeight() - margin;
                    content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), fontSize);
                }
                String[] values = {
                        String.valueOf(dto.empId()), nullSafe(dto.name()), nullSafe(dto.email()),
                        nullSafe(dto.departmentName()), dto.salary() != null ? String.valueOf(dto.salary()) : ""
                };
                y = drawRow(content, values, colWidths, margin, y, rowHeight);
            }

            content.close();
            document.save(out);
            return out.toByteArray();
        }
    }

    private float drawRow(PDPageContentStream content, String[] values, float[] widths, float startX, float y, float rowHeight) throws IOException {
        float x = startX;
        for (int i = 0; i < values.length; i++) {
            content.beginText();
            content.newLineAtOffset(x, y);
            String text = values[i] == null ? "" : values[i];
            if (text.length() > 30) text = text.substring(0, 27) + "...";
            content.showText(text);
            content.endText();
            x += widths[i];
        }
        return y - rowHeight;
    }
}