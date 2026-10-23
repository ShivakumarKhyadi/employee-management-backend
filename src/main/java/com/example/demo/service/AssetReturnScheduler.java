package com.example.demo.service;

import com.example.demo.model.CompanyAsset;
import com.example.demo.model.Employee;
import com.example.demo.repository.CompanyAssetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class AssetReturnScheduler {

    @Autowired private CompanyAssetRepository assetRepository;
    @Autowired private NotificationService notificationService;

    @Scheduled(cron = "0 30 8 * * *")
    public void checkReturnsDue() {
        List<CompanyAsset> assets = assetRepository.findByReturnDueDateIsNotNullAndReturnNotifiedFalse();
        LocalDate today = LocalDate.now();

        for (CompanyAsset asset : assets) {
            long daysLeft = ChronoUnit.DAYS.between(today, asset.getReturnDueDate());
            if (daysLeft > 7) continue;

            Employee employee = asset.getEmployee();
            if (employee == null) continue;

            String statusText = daysLeft < 0 ? "was due back" : "is due back in " + daysLeft + " days";

            if (employee.getAppUser() != null) {
                notificationService.notify(
                        employee.getAppUser().getUsername(),
                        "ASSET_RETURN_DUE",
                        asset.getAssetName() + " " + statusText,
                        "/assets"
                );
            }
            notificationService.notifyAdminsAndSupervisors(
                    "ASSET_RETURN_DUE",
                    employee.getName() + "'s " + asset.getAssetName() + " " + statusText,
                    "/assets"
            );

            asset.setReturnNotified(true);
            assetRepository.save(asset);
        }
    }
}