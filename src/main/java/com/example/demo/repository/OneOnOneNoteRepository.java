package com.example.demo.repository;

import com.example.demo.model.OneOnOneNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OneOnOneNoteRepository extends JpaRepository<OneOnOneNote, Long> {
    List<OneOnOneNote> findByEmployeeIdOrderByMeetingDateDesc(Long employeeId);
}