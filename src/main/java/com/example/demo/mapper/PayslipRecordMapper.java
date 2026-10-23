package com.example.demo.mapper;

import com.example.demo.dto.PayslipRecordDTO;
import com.example.demo.model.PayslipRecord;

public class PayslipRecordMapper {

    public static PayslipRecordDTO toDto(PayslipRecord p){
        return new PayslipRecordDTO(
                p.getId(),
                p.getPayMonth(),
                p.getBasicPay(),
                p.getHra(),
                p.getOtherAllowance(),
                p.getGrossPay(),
                p.getProvidentFund(),
                p.getProfessionalTax(),
                p.getNetPay(),
                p.getGeneratedBy(),
                p.getGeneratedDate()
        );
    }
}