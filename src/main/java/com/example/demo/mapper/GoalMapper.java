package com.example.demo.mapper;

import com.example.demo.dto.KeyResultDTO;
import com.example.demo.dto.ObjectiveDTO;
import com.example.demo.dto.OneOnOneNoteDTO;
import com.example.demo.model.KeyResult;
import com.example.demo.model.Objective;
import com.example.demo.model.OneOnOneNote;

import java.util.List;
import java.util.stream.Collectors;

public class GoalMapper {

    public static KeyResultDTO toDto(KeyResult kr) {
        return new KeyResultDTO(kr.getId(), kr.getDescription(), kr.getTargetValue(), kr.getCurrentValue(), kr.getUnit());
    }

    public static ObjectiveDTO toDto(Objective o, String employeeName) {
        List<KeyResultDTO> krDtos = o.getKeyResults().stream().map(GoalMapper::toDto).collect(Collectors.toList());

        double progress = krDtos.isEmpty() ? 0.0 : krDtos.stream()
                                                   .mapToDouble(kr -> kr.targetValue() == 0 ? 0 : Math.min(100.0, (kr.currentValue() / kr.targetValue()) * 100.0))
                                                   .average().orElse(0.0);

        return new ObjectiveDTO(
                o.getId(), o.getEmployeeId(), employeeName, o.getTitle(), o.getDescription(),
                o.getQuarter(), o.getStatus(), krDtos, Math.round(progress * 10) / 10.0
        );
    }

    public static OneOnOneNoteDTO toDto(OneOnOneNote n, String employeeName) {
        return new OneOnOneNoteDTO(
                n.getId(), n.getEmployeeId(), employeeName, n.getManagerUsername(), n.getMeetingDate(),
                n.getDiscussionNotes(), n.getActionItems(), n.getCreatedBy(), n.getCreatedDate()
        );
    }
}