package com.example.demo.mapper;

import com.example.demo.dto.PayrollEntryDTO;
import com.example.demo.dto.PayrollRunDTO;
import com.example.demo.dto.SalaryComponentDTO;
import com.example.demo.model.PayrollEntry;
import com.example.demo.model.PayrollRun;
import com.example.demo.model.SalaryComponent;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class PayrollMapper {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static SalaryComponentDTO toDto(SalaryComponent c) {
        return new SalaryComponentDTO(c.getId(), c.getName(), c.getComponentType(), c.getCalculationType(), c.getDefaultValue(), c.isActive());
    }

    public static PayrollRunDTO toDto(PayrollRun r) {
        return new PayrollRunDTO(r.getId(), r.getPayMonth(), r.getStatus(), r.getRunBy(), r.getRunDate(), r.getEmployeeCount());
    }

    public static PayrollEntryDTO toDto(PayrollEntry e, String employeeName, String payMonth) {
        List<PayrollEntryDTO.ComponentLine> breakdown;
        try {
            breakdown = OBJECT_MAPPER.readValue(e.getBreakdownJson(),
                    OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, PayrollEntryDTO.ComponentLine.class));
        } catch (Exception ex) {
            breakdown = List.of();
        }
        return new PayrollEntryDTO(e.getId(), e.getEmployeeId(), employeeName, payMonth,
                e.getBasicPay(), e.getGrossPay(), e.getTotalDeductions(), e.getNetPay(), breakdown);
    }
}