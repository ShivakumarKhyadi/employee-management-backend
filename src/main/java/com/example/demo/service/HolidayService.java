package com.example.demo.service;

import com.example.demo.dto.HolidayDTO;
import com.example.demo.exception.HolidayNotFoundException;
import com.example.demo.mapper.HolidayMapper;
import com.example.demo.model.Holiday;
import com.example.demo.repository.HolidayRepository;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HolidayService {

    @Autowired private HolidayRepository holidayRepository;

    public record CreateHolidayRequest(
            @NotBlank(message = "Name is required") String name,
            @NotNull(message = "Date is required") LocalDate date,
            String description,
            boolean optional
    ) {}

    public List<HolidayDTO> getAll() {
        return holidayRepository.findAllByOrderByDateAsc().stream()
                .map(HolidayMapper::toDto).collect(Collectors.toList());
    }

    public List<HolidayDTO> getForRange(LocalDate start, LocalDate end) {
        return holidayRepository.findByDateBetweenOrderByDateAsc(start, end).stream()
                .map(HolidayMapper::toDto).collect(Collectors.toList());
    }

    public HolidayDTO create(CreateHolidayRequest request) {
        Holiday holiday = new Holiday();
        holiday.setName(request.name().trim());
        holiday.setDate(request.date());
        holiday.setDescription(request.description());
        holiday.setOptional(request.optional());
        return HolidayMapper.toDto(holidayRepository.save(holiday));
    }

    public void delete(Long id) {
        if (!holidayRepository.existsById(id)) {
            throw new HolidayNotFoundException("Holiday not found");
        }
        holidayRepository.deleteById(id);
    }
}