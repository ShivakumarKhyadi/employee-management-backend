package com.example.demo.service;

import com.example.demo.dto.ObjectiveDTO;
import com.example.demo.dto.OneOnOneNoteDTO;
import com.example.demo.exception.EmployeeNotFoundException;
import com.example.demo.exception.ObjectiveNotFoundException;
import com.example.demo.mapper.GoalMapper;
import com.example.demo.model.*;
import com.example.demo.repository.EmployeeRepository;
import com.example.demo.repository.KeyResultRepository;
import com.example.demo.repository.ObjectiveRepository;
import com.example.demo.repository.OneOnOneNoteRepository;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoalService {

    @Autowired private ObjectiveRepository objectiveRepository;
    @Autowired private KeyResultRepository keyResultRepository;
    @Autowired private OneOnOneNoteRepository noteRepository;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private NotificationService notificationService;

    public record KeyResultInput(@NotBlank String description, @NotNull Double targetValue, String unit) {}

    public record CreateObjectiveRequest(
            @NotBlank(message = "Title is required") String title,
            String description,
            @NotBlank(message = "Quarter is required") String quarter,
            List<KeyResultInput> keyResults
    ) {}

    public record UpdateKeyResultRequest(@NotNull Double currentValue) {}
    public record UpdateStatusRequest(@NotNull GoalStatus status) {}

    public record CreateNoteRequest(
            @NotNull(message = "Employee is required") Long employeeId,
            @NotNull(message = "Meeting date is required") LocalDate meetingDate,
            String discussionNotes,
            String actionItems
    ) {}


    public List<ObjectiveDTO> getObjectivesForEmployee(Long employeeId) {
        String name = nameOf(employeeId);
        return objectiveRepository.findByEmployeeIdOrderByQuarterDesc(employeeId).stream()
                .map(o -> GoalMapper.toDto(o, name)).collect(Collectors.toList());
    }

    public ObjectiveDTO createObjective(Long employeeId, CreateObjectiveRequest request) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));

        Objective objective = new Objective();
        objective.setEmployeeId(employeeId);
        objective.setTitle(request.title().trim());
        objective.setDescription(request.description());
        objective.setQuarter(request.quarter());
        objective.setStatus(GoalStatus.NOT_STARTED);
        objective = objectiveRepository.save(objective);

        if (request.keyResults() != null) {
            for (KeyResultInput input : request.keyResults()) {
                KeyResult kr = new KeyResult();
                kr.setObjective(objective);
                kr.setDescription(input.description());
                kr.setTargetValue(input.targetValue());
                kr.setCurrentValue(0.0);
                kr.setUnit(input.unit());
                keyResultRepository.save(kr);
                objective.getKeyResults().add(kr);
            }
        }

        if (employee.getAppUser() != null) {
            notificationService.notify(employee.getAppUser().getUsername(), "GOAL_ASSIGNED",
                    "A new goal was set for you: " + objective.getTitle(), "/goals");
        }

        return GoalMapper.toDto(objective, employee.getName());
    }

    public void updateKeyResultProgress(Long keyResultId, UpdateKeyResultRequest request) {
        KeyResult kr = keyResultRepository.findById(keyResultId)
                .orElseThrow(() -> new ObjectiveNotFoundException("Key result not found"));
        kr.setCurrentValue(request.currentValue());
        keyResultRepository.save(kr);
    }

    public ObjectiveDTO updateObjectiveStatus(Long objectiveId, UpdateStatusRequest request) {
        Objective objective = objectiveRepository.findById(objectiveId)
                .orElseThrow(() -> new ObjectiveNotFoundException("Objective not found"));
        objective.setStatus(request.status());
        Objective saved = objectiveRepository.save(objective);
        return GoalMapper.toDto(saved, nameOf(saved.getEmployeeId()));
    }

    public void deleteObjective(Long objectiveId) {
        if (!objectiveRepository.existsById(objectiveId)) {
            throw new ObjectiveNotFoundException("Objective not found");
        }
        objectiveRepository.deleteById(objectiveId);
    }


    public List<OneOnOneNoteDTO> getNotesForEmployee(Long employeeId) {
        String name = nameOf(employeeId);
        return noteRepository.findByEmployeeIdOrderByMeetingDateDesc(employeeId).stream()
                .map(n -> GoalMapper.toDto(n, name)).collect(Collectors.toList());
    }

    public OneOnOneNoteDTO addNote(String managerUsername, CreateNoteRequest request) {
        Employee employee = employeeRepository.findById(request.employeeId())
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));

        OneOnOneNote note = new OneOnOneNote();
        note.setEmployeeId(request.employeeId());
        note.setManagerUsername(managerUsername);
        note.setMeetingDate(request.meetingDate());
        note.setDiscussionNotes(request.discussionNotes());
        note.setActionItems(request.actionItems());
        OneOnOneNote saved = noteRepository.save(note);

        return GoalMapper.toDto(saved, employee.getName());
    }

    public void deleteNote(Long noteId) {
        if (!noteRepository.existsById(noteId)) {
            throw new ObjectiveNotFoundException("Note not found");
        }
        noteRepository.deleteById(noteId);
    }

    private String nameOf(Long employeeId) {
        return employeeRepository.findById(employeeId).map(Employee::getName).orElse("Unknown");
    }
}