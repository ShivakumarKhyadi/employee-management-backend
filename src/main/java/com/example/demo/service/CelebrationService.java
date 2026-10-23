package com.example.demo.service;

import com.example.demo.dto.CelebrationDTO;
import com.example.demo.model.Employee;
import com.example.demo.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.MonthDay;
import java.time.Period;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class CelebrationService {

    @Autowired private EmployeeRepository employeeRepository;

    public List<CelebrationDTO> getUpcoming(int daysAhead) {
        List<Employee> all = employeeRepository.findAll();
        LocalDate today = LocalDate.now();
        List<CelebrationDTO> results = new ArrayList<>();

        for (Employee e : all) {
            addIfWithinRange(results, e, e.getDateOfBirth(), "BIRTHDAY", today, daysAhead);
            addIfWithinRange(results, e, e.getJoiningDate(), "ANNIVERSARY", today, daysAhead);
        }

        results.sort(Comparator.comparing(c -> daysUntil(c.date())));
        return results;
    }

    private void addIfWithinRange(List<CelebrationDTO> results, Employee employee, LocalDate sourceDate,
                                  String type, LocalDate today, int daysAhead) {
        if (sourceDate == null) return;

        MonthDay monthDay = MonthDay.from(sourceDate);
        LocalDate nextOccurrence = monthDay.atYear(today.getYear());
        if (nextOccurrence.isBefore(today)) {
            nextOccurrence = monthDay.atYear(today.getYear() + 1);
        }

        long days = daysUntil(nextOccurrence);
        if (days > daysAhead) return;

        int years = Period.between(sourceDate, nextOccurrence).getYears();
        if (type.equals("ANNIVERSARY") && years <= 0) return; // skip someone who joined today this year

        results.add(new CelebrationDTO(employee.getEmpId(), employee.getName(), type, nextOccurrence, years, days == 0));
    }

    private long daysUntil(LocalDate date) {
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), date);
    }
}