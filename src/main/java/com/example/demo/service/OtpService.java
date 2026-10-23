package com.example.demo.service;

import com.example.demo.model.OtpToken;
import com.example.demo.repository.OtpTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class OtpService {

    private static final int OTP_VALID_MINUTES = 5;

    @Autowired private OtpTokenRepository otpTokenRepository;

    public String generateAndSend(String username, String mobileNumber){
        String code = String.format("%06d", new Random().nextInt(1_000_000));

        OtpToken otp = new OtpToken();
        otp.setUsername(username);
        otp.setOtpCode(code);
        otp.setExpiryDate(LocalDateTime.now().plusMinutes(OTP_VALID_MINUTES));
        otp.setUsed(false);
        otp.setCreatedDate(LocalDateTime.now());
        otpTokenRepository.save(otp);

        System.out.println("[OTP] Sending code " + code + " to " + mobileNumber + " for user " + username);
        return code;
    }

    public boolean verify(String username, String submittedCode){
        return otpTokenRepository.findTopByUsernameAndUsedFalseOrderByCreatedDateDesc(username)
                .filter(otp -> otp.getOtpCode().equals(submittedCode))
                .filter(otp -> otp.getExpiryDate().isAfter(LocalDateTime.now()))
                .map(otp -> {
                    otp.setUsed(true);
                    otpTokenRepository.save(otp);
                    return true;
                })
                .orElse(false);
    }
}
