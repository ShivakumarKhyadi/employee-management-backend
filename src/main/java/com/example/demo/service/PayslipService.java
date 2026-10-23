package com.example.demo.service;

import com.example.demo.dto.PayslipRecordDTO;
import com.example.demo.mapper.PayslipRecordMapper;
import com.example.demo.model.Employee;
import com.example.demo.model.PayslipRecord;
import com.example.demo.repository.EmployeeRepository;
import com.example.demo.repository.PayslipRecordRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class PayslipService {

    private static final String STORAGE_DIR = "uploads/payslips/";

    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private PayslipRecordRepository payslipRecordRepository;

    public PayslipRecordDTO generatePayslip(Long employeeId, String monthStr, String generatedByUsername)throws IOException{
        Employee employee = employeeRepository.findById(employeeId)
        .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        YearMonth month = YearMonth.parse(monthStr);

        double salary = employee.getSalary() != null ? employee.getSalary() : 0.0;
        double basic = salary * 0.60;
        double hra = salary * 0.20;
        double otherAllowance = salary * 0.20;
        double gross = basic + hra + otherAllowance;

        double pf = basic * 0.12;
        double professionalTax = 200.0;
        double totalDeductions = pf + professionalTax;

        double netPay = gross - totalDeductions;

        byte[] pdfBytes = buildPdf(employee, month, basic, hra, otherAllowance, gross, pf, professionalTax, totalDeductions, netPay);

        Files.createDirectories(Paths.get(STORAGE_DIR));
        String filename = employeeId + "_" + monthStr + "_" + System.currentTimeMillis() + ".pdf";
        Path path = Paths.get(STORAGE_DIR, filename);
        Files.write(path, pdfBytes);

        PayslipRecord record = payslipRecordRepository.findByEmployeeIdAndPayMonth(employeeId, monthStr)
                .orElseGet(PayslipRecord::new);

        if(record.getId() != null && record.getFilePath() != null){
            Files.deleteIfExists(Paths.get(record.getFilePath()));
        }

        record.setEmployeeId(employeeId);
        record.setPayMonth(monthStr);
        record.setBasicPay(basic);
        record.setHra(hra);
        record.setOtherAllowance(otherAllowance);
        record.setGrossPay(gross);
        record.setProvidentFund(pf);
        record.setProfessionalTax(professionalTax);
        record.setNetPay(netPay);
        record.setFilePath(path.toString());
        record.setGeneratedBy(generatedByUsername);
        record.setGeneratedDate(LocalDateTime.now());
        record = payslipRecordRepository.save(record);

        return PayslipRecordMapper.toDto(record);
    }

    public List<PayslipRecordDTO> listForEmployee(Long employeeId){
        return payslipRecordRepository.findByEmployeeIdOrderByPayMonthDesc(employeeId).stream()
                .map(PayslipRecordMapper::toDto)
                .collect(Collectors.toList());
    }

    public PayslipRecord getRecord(Long id){
        return payslipRecordRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payslip not found"));
    }

    private byte[] buildPdf(Employee employee, YearMonth month, double basic, double hra, double otherAllowance,
                            double gross, double pf, double professionalTax, double totalDeductions, double netPay) throws IOException{
        try(PDDocument document = new PDDocument()){
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            var titleFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            var headingFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            var bodyFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            float margin = 50;
            float y = PDRectangle.A4.getHeight() - margin;
            float width = PDRectangle.A4.getWidth() - 2 * margin;

            try(PDPageContentStream cs = new PDPageContentStream(document, page)){
                cs.beginText();
                cs.setFont(titleFont, 18);
                cs.newLineAtOffset(margin, y);
                cs.showText("SEN-SEI Technologies Pvt Ltd");
                cs.endText();
                y -= 20;

                cs.beginText();
                cs.setFont(bodyFont, 10);
                cs.newLineAtOffset(margin, y);
                cs.showText("Payslip for " + month.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + month.getYear());
                cs.endText();
                y -= 30;

                cs.setLineWidth(0.8f);
                cs.moveTo(margin, y);
                cs.lineTo(margin + width, y);
                cs.stroke();
                y -= 25;

                cs.beginText();
                cs.setFont(headingFont, 11);
                cs.newLineAtOffset(margin, y);
                cs.showText("Employee Details");
                cs.endText();
                y -= 18;

                String deptName = employee.getDepartment() != null ? employee.getDepartment().getDepartmentName() : "-";
                String[][] empRows = {
                        {"Name", employee.getName() != null ? employee.getName() : "-"},
                        {"Employee ID", String.valueOf(employee.getEmpId())},
                        {"Department", deptName},
                        {"Email", employee.getEmail() != null ? employee.getEmail() : "-"},
                };
                for (String[] row : empRows) {
                    cs.beginText();
                    cs.setFont(bodyFont, 10);
                    cs.newLineAtOffset(margin, y);
                    cs.showText(row[0] + ":");
                    cs.endText();
                    cs.beginText();
                    cs.setFont(bodyFont, 10);
                    cs.newLineAtOffset(margin + 130, y);
                    cs.showText(row[1]);
                    cs.endText();
                    y -= 16;
                }
                y -= 15;

                cs.beginText();
                cs.setFont(headingFont, 11);
                cs.newLineAtOffset(margin, y);
                cs.showText("Earnings");
                cs.endText();
                cs.beginText();
                cs.setFont(headingFont, 11);
                cs.newLineAtOffset(margin + width / 2, y);
                cs.showText("Deductions");
                cs.endText();
                y -= 18;

                String[][] earnings = {
                        {"Basic", formatInr(basic)},
                        {"HRA", formatInr(hra)},
                        {"Other Allowance", formatInr(otherAllowance)},
                };
                String[][] deductions = {
                        {"Provident Fund", formatInr(pf)},
                        {"Professional Tax", formatInr(professionalTax)},
                };

                int maxRows = Math.max(earnings.length, deductions.length);
                for (int i = 0; i < maxRows; i++) {
                    if (i < earnings.length) {
                        cs.beginText();
                        cs.setFont(bodyFont, 10);
                        cs.newLineAtOffset(margin, y);
                        cs.showText(earnings[i][0] + "   " + earnings[i][1]);
                        cs.endText();
                    }
                    if (i < deductions.length) {
                        cs.beginText();
                        cs.setFont(bodyFont, 10);
                        cs.newLineAtOffset(margin + width / 2, y);
                        cs.showText(deductions[i][0] + "   " + deductions[i][1]);
                        cs.endText();
                    }
                    y -= 16;
                }
                y -= 10;

                cs.setLineWidth(0.6f);
                cs.moveTo(margin, y);
                cs.lineTo(margin + width, y);
                cs.stroke();
                y -= 20;

                cs.beginText();
                cs.setFont(headingFont, 10);
                cs.newLineAtOffset(margin, y);
                cs.showText("Gross Pay: " + formatInr(gross));
                cs.endText();
                cs.beginText();
                cs.setFont(headingFont, 10);
                cs.newLineAtOffset(margin + width / 2, y);
                cs.showText("Total Deductions: " + formatInr(totalDeductions));
                cs.endText();
                y -= 30;

                cs.setLineWidth(1.2f);
                cs.moveTo(margin, y);
                cs.lineTo(margin + width, y);
                cs.stroke();
                y -= 22;

                cs.beginText();
                cs.setFont(titleFont, 13);
                cs.newLineAtOffset(margin, y);
                cs.showText("Net Pay: " + formatInr(netPay));
                cs.endText();
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        }
    }

    private String formatInr(double amount) {
        return String.format(Locale.US, "Rs. %,.2f", amount);
    }
}
