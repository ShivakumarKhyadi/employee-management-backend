package com.example.demo.service;

import com.example.demo.model.Employee;
import com.example.demo.model.FileEntity;
import com.example.demo.repository.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class DocumentExpiryScheduler {

    @Autowired private FileRepository fileRepository;
    @Autowired private NotificationService notificationService;
    @Autowired private EmailService emailService;

    @Scheduled(cron = "0 0 8 * * *")
    public void checkExpiringDocuments() {
        List<FileEntity> files = fileRepository.findAll();
        LocalDate today = LocalDate.now();

        for (FileEntity f : files) {
            if (f.getExpiryDate() == null || f.isExpiryNotified()) continue;

            long daysLeft = ChronoUnit.DAYS.between(today, f.getExpiryDate());
            if (daysLeft > 15) continue;

            Employee employee = f.getEmployee();
            String statusText = daysLeft < 0 ? "has expired" : "expires in " + daysLeft + " days";

            if (employee.getAppUser() != null) {
                notificationService.notify(
                        employee.getAppUser().getUsername(),
                        "DOCUMENT_EXPIRY",
                        f.getFilename() + " " + statusText,
                        "/employees/" + employee.getEmpId()
                );
            }
            notificationService.notifyAdminsAndSupervisors(
                    "DOCUMENT_EXPIRY",
                    employee.getName() + "'s document (" + f.getFilename() + ") " + statusText,
                    "/employees/" + employee.getEmpId()
            );
            if (employee.getEmail() != null) {
                emailService.send(
                        employee.getEmail(),
                        "Document " + (daysLeft < 0 ? "expired" : "expiring soon"),
                        "Hello " + employee.getName() + ",\n\nYour document '" + f.getFilename() + "' " + statusText + ". Please renew it as soon as possible."
                );
            }

            f.setExpiryNotified(true);
            fileRepository.save(f);
        }
    }
}