package com.example.demo.service;

import com.example.demo.dto.ImportResultDTO;
import com.example.demo.dto.ImportRowError;
import com.example.demo.model.Department;
import com.example.demo.model.Employee;
import com.example.demo.model.EmployeeType;
import com.example.demo.model.Grade;
import com.example.demo.repository.DepartmentRepository;
import com.example.demo.repository.EmployeeRepository;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.nio.charset.StandardCharsets;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmployeeExcelImportService {

    @Autowired private EmployeeService employeeService;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private DepartmentRepository departmentRepository;

    public ImportResultDTO importFromExcel(MultipartFile file) throws IOException {
        List<ImportRowError> errors = new ArrayList<>();
        int imported = 0;
        int totalDataRows = 0;

        try (InputStream is = file.getInputStream(); Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet.getPhysicalNumberOfRows() == 0) {
                return new ImportResultDTO(0, 0, 0, List.of(new ImportRowError(0, "The sheet is empty")));
            }

            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            Map<String, Integer> columnIndex = mapHeaders(headerRow);

            String[] required = { "name", "email", "department", "joiningdate" };
            for (String req : required) {
                if (!columnIndex.containsKey(req)) {
                    return new ImportResultDTO(0, 0, 0, List.of(new ImportRowError(0,
                            "Missing required column: '" + req + "' (check the header row spelling)")));
                }
            }

            for (int r = headerRow.getRowNum() + 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null || isRowBlank(row)) continue;

                int displayRowNumber = r + 1;
                totalDataRows++;

                try {
                    Employee employee = buildEmployeeFromRow(row, columnIndex);
                    employeeService.createEmployee(employee);
                    imported++;
                } catch (RowValidationException ex) {
                    errors.add(new ImportRowError(displayRowNumber, ex.getMessage()));
                } catch (Exception ex) {
                    errors.add(new ImportRowError(displayRowNumber, "Unexpected error: " + ex.getMessage()));
                }
            }
        }

        return new ImportResultDTO(totalDataRows, imported, errors.size(), errors);
    }

    private Employee buildEmployeeFromRow(Row row, Map<String, Integer> col) {
        String name = getString(row, col, "name");
        if (name == null || name.isBlank()) {
            throw new RowValidationException("Name is required");
        }

        String email = getString(row, col, "email");
        if (email == null || email.isBlank()) {
            throw new RowValidationException("Email is required");
        }
        if (employeeRepository.findByEmail(email).isPresent()) {
            throw new RowValidationException("Email '" + email + "' already exists");
        }

        String departmentName = getString(row, col, "department");
        if (departmentName == null || departmentName.isBlank()) {
            throw new RowValidationException("Department is required");
        }
        Department department = departmentRepository.findByDepartmentNameIgnoreCase(departmentName.trim())
                .orElseThrow(() -> new RowValidationException("Department '" + departmentName + "' not found"));

        LocalDate joiningDate = getDate(row, col, "joiningdate");
        if (joiningDate == null) {
            throw new RowValidationException("Joining date is required or unreadable — use a real date cell or yyyy-MM-dd text");
        }

        Double salary = getNumeric(row, col, "salary");

        EmployeeType employeeType = EmployeeType.EMPLOYEE;
        String employeeTypeStr = getString(row, col, "employeetype");
        if (employeeTypeStr != null && !employeeTypeStr.isBlank()) {
            try {
                employeeType = EmployeeType.valueOf(employeeTypeStr.trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new RowValidationException("Employee type '" + employeeTypeStr + "' is invalid (expected EMPLOYEE or AGENT)");
            }
        }

        Grade grade = null;
        String gradeStr = getString(row, col, "grade");
        if (gradeStr != null && !gradeStr.isBlank()) {
            try {
                grade = Grade.valueOf(gradeStr.trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new RowValidationException("Grade '" + gradeStr + "' is invalid (expected JUNIOR, MID, SENIOR, or LEAD)");
            }
        }

        boolean supervisor = getBoolean(row, col, "issupervisor");

        Employee employee = new Employee();
        employee.setName(name.trim());
        employee.setEmail(email.trim());
        employee.setSalary(salary);
        employee.setJoiningDate(joiningDate);
        employee.setEmployeeType(employeeType);
        employee.setGrade(grade);
        employee.setSupervisor(supervisor);
        employee.setDepartmentId(department.getDeptId());
        return employee;
    }

    private Map<String, Integer> mapHeaders(Row headerRow) {
        Map<String, Integer> map = new HashMap<>();
        for (Cell cell : headerRow) {
            String key = cell.getStringCellValue().trim().toLowerCase().replaceAll("[\\s_]+", "");
            map.put(key, cell.getColumnIndex());
        }
        return map;
    }

    private boolean isRowBlank(Row row) {
        for (Cell cell : row) {
            if (cell.getCellType() != CellType.BLANK && !getCellAsString(cell).isBlank()) return false;
        }
        return true;
    }

    private String getString(Row row, Map<String, Integer> col, String key) {
        Integer idx = col.get(key);
        if (idx == null) return null;
        Cell cell = row.getCell(idx, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        String value = getCellAsString(cell);
        return value.isBlank() ? null : value;
    }

    private Double getNumeric(Row row, Map<String, Integer> col, String key) {
        Integer idx = col.get(key);
        if (idx == null) return null;
        Cell cell = row.getCell(idx, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        if (cell.getCellType() == CellType.NUMERIC) return cell.getNumericCellValue();
        String s = getCellAsString(cell);
        if (s.isBlank()) return null;
        try {
            return Double.parseDouble(s.trim());
        } catch (NumberFormatException ex) {
            throw new RowValidationException("Salary '" + s + "' is not a valid number");
        }
    }

    private LocalDate getDate(Row row, Map<String, Integer> col, String key) {
        Integer idx = col.get(key);
        if (idx == null) return null;
        Cell cell = row.getCell(idx, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toLocalDate();
        }
        String s = getCellAsString(cell);
        if (s.isBlank()) return null;
        try {
            return LocalDate.parse(s.trim());
        } catch (Exception ex) {
            return null;
        }
    }

    private boolean getBoolean(Row row, Map<String, Integer> col, String key) {
        Integer idx = col.get(key);
        if (idx == null) return false;
        Cell cell = row.getCell(idx, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        if (cell.getCellType() == CellType.BOOLEAN) return cell.getBooleanCellValue();
        String s = getCellAsString(cell).trim().toLowerCase();
        return s.equals("true") || s.equals("yes") || s.equals("1");
    }

    private String getCellAsString(Cell cell) {
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                double d = cell.getNumericCellValue();
                yield (d == Math.floor(d)) ? String.valueOf((long) d) : String.valueOf(d);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }

    private static class RowValidationException extends RuntimeException {
        RowValidationException(String message) { super(message); }
    }

    public ImportResultDTO importFromFile(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();
        if (filename != null && filename.toLowerCase().endsWith(".csv")) {
            return importFromCsv(file);
        }
        return importFromExcel(file); // existing method, unchanged
    }

    private ImportResultDTO importFromCsv(MultipartFile file) throws IOException {
        List<ImportRowError> errors = new ArrayList<>();
        int imported = 0;
        int totalDataRows = 0;

        try (var reader = new java.io.BufferedReader(new java.io.InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return new ImportResultDTO(0, 0, 0, List.of(new ImportRowError(0, "The file is empty")));
            }

            String[] headers = headerLine.split(",");
            Map<String, Integer> columnIndex = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                String key = headers[i].trim().toLowerCase().replaceAll("[\\s_\"]+", "");
                columnIndex.put(key, i);
            }

            String[] required = { "name", "email", "department", "joiningdate" };
            for (String req : required) {
                if (!columnIndex.containsKey(req)) {
                    return new ImportResultDTO(0, 0, 0, List.of(new ImportRowError(0,
                            "Missing required column: '" + req + "' (check the header row spelling)")));
                }
            }

            String line;
            int rowNum = 1;
            while ((line = reader.readLine()) != null) {
                rowNum++;
                if (line.isBlank()) continue;
                totalDataRows++;

                String[] cells = line.split(",", -1);
                try {
                    Employee employee = buildEmployeeFromCsvRow(cells, columnIndex);
                    employeeService.createEmployee(employee);
                    imported++;
                } catch (RowValidationException ex) {
                    errors.add(new ImportRowError(rowNum, ex.getMessage()));
                } catch (Exception ex) {
                    errors.add(new ImportRowError(rowNum, "Unexpected error: " + ex.getMessage()));
                }
            }
        }

        return new ImportResultDTO(totalDataRows, imported, errors.size(), errors);
    }

    private String csvCell(String[] cells, Map<String, Integer> col, String key) {
        Integer idx = col.get(key);
        if (idx == null || idx >= cells.length) return null;
        String value = cells[idx].trim().replaceAll("^\"|\"$", "");
        return value.isBlank() ? null : value;
    }

    private Employee buildEmployeeFromCsvRow(String[] cells, Map<String, Integer> col) {
        String name = csvCell(cells, col, "name");
        if (name == null) throw new RowValidationException("Name is required");

        String email = csvCell(cells, col, "email");
        if (email == null) throw new RowValidationException("Email is required");
        if (employeeRepository.findByEmail(email).isPresent()) {
            throw new RowValidationException("Email '" + email + "' already exists");
        }

        String departmentName = csvCell(cells, col, "department");
        if (departmentName == null) throw new RowValidationException("Department is required");
        Department department = departmentRepository.findByDepartmentNameIgnoreCase(departmentName)
                .orElseThrow(() -> new RowValidationException("Department '" + departmentName + "' not found"));

        String joiningDateStr = csvCell(cells, col, "joiningdate");
        LocalDate joiningDate;
        try {
            joiningDate = LocalDate.parse(joiningDateStr);
        } catch (Exception ex) {
            throw new RowValidationException("Joining date is required or unreadable — use yyyy-MM-dd");
        }

        Double salary = null;
        String salaryStr = csvCell(cells, col, "salary");
        if (salaryStr != null) {
            try {
                salary = Double.parseDouble(salaryStr);
            } catch (NumberFormatException ex) {
                throw new RowValidationException("Salary '" + salaryStr + "' is not a valid number");
            }
        }

        EmployeeType employeeType = EmployeeType.EMPLOYEE;
        String typeStr = csvCell(cells, col, "employeetype");
        if (typeStr != null) {
            try {
                employeeType = EmployeeType.valueOf(typeStr.toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new RowValidationException("Employee type '" + typeStr + "' is invalid");
            }
        }

        Grade grade = null;
        String gradeStr = csvCell(cells, col, "grade");
        if (gradeStr != null) {
            try {
                grade = Grade.valueOf(gradeStr.toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new RowValidationException("Grade '" + gradeStr + "' is invalid");
            }
        }

        boolean supervisor = false;
        String supStr = csvCell(cells, col, "issupervisor");
        if (supStr != null) {
            String s = supStr.toLowerCase();
            supervisor = s.equals("true") || s.equals("yes") || s.equals("1");
        }

        Employee employee = new Employee();
        employee.setName(name);
        employee.setEmail(email);
        employee.setSalary(salary);
        employee.setJoiningDate(joiningDate);
        employee.setEmployeeType(employeeType);
        employee.setGrade(grade);
        employee.setSupervisor(supervisor);
        employee.setDepartmentId(department.getDeptId());
        return employee;
    }
}