package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired private JavaMailSender mailSender;

    @Async
    public void sendResetEmail(String to, String username, String link, int validMinutes) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Reset your EMS password");
        message.setText(
                "Hello " + username + ",\n\n" +
                        "We received a request to reset your password. Click the link below to choose a new one:\n\n" +
                        link + "\n\n" +
                        "This link expires in " + validMinutes + " minutes. If you didn't request this, you can safely ignore this email."
        );
        mailSender.send(message);
    }

    @Async
    public void sendWelcomeEmail(String to, String username, String link, int validMinutes) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Welcome to EMS — set your password");
        message.setText(
                "Hello " + username + ",\n\n" +
                        "An account has been created for you in the Employee Management System. " +
                        "Click the link below to set your password and get started:\n\n" +
                        link + "\n\n" +
                        "This link expires in " + (validMinutes / 60) + " hours. If you weren't expecting this, please contact your administrator."
        );
        mailSender.send(message);
    }

    @Async
    public void send(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }
}