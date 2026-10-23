package com.example.demo.mapper;

import com.example.demo.dto.AnnouncementDTO;
import com.example.demo.model.Announcement;

public class AnnouncementMapper {
    public static AnnouncementDTO toDto(Announcement a) {
        return new AnnouncementDTO(
                a.getId(), a.getTitle(), a.getMessage(), a.getCategory(), a.isPinned(),
                a.getCreatedBy(), a.getCreatedDate(), a.getLastModifiedBy(), a.getLastModifiedDate()
        );
    }
}