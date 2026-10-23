package com.example.demo.service;

import com.example.demo.dto.AnnouncementDTO;
import com.example.demo.exception.AnnouncementNotFoundException;
import com.example.demo.mapper.AnnouncementMapper;
import com.example.demo.model.Announcement;
import com.example.demo.model.AnnouncementCategory;
import com.example.demo.repository.AnnouncementRepository;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnnouncementFeedService {

    @Autowired private AnnouncementRepository announcementRepository;
    @Autowired private NotificationService notificationService;

    public record CreateAnnouncementRequest(
            @NotBlank(message = "Title is required") String title,
            @NotBlank(message = "Message is required")
            @Size(max = 3000, message = "Message must be under 3000 characters") String message,
            @NotNull(message = "Category is required") AnnouncementCategory category,
            boolean pinned
    ) {}

    public List<AnnouncementDTO> getAll() {
        return announcementRepository.findAllByOrderByPinnedDescCreatedDateDesc().stream()
                .map(AnnouncementMapper::toDto).collect(Collectors.toList());
    }

    public AnnouncementDTO create(CreateAnnouncementRequest request) {
        Announcement announcement = new Announcement();
        announcement.setTitle(request.title().trim());
        announcement.setMessage(request.message().trim());
        announcement.setCategory(request.category());
        announcement.setPinned(request.pinned());
        Announcement saved = announcementRepository.save(announcement);

        notificationService.notifyAllUsers(
                request.category().name(),
                request.title().trim(),
                "/company-feed"
        );

        return AnnouncementMapper.toDto(saved);
    }

    public AnnouncementDTO update(Long id, CreateAnnouncementRequest request) {
        Announcement announcement = findOrThrow(id);
        announcement.setTitle(request.title().trim());
        announcement.setMessage(request.message().trim());
        announcement.setCategory(request.category());
        announcement.setPinned(request.pinned());
        return AnnouncementMapper.toDto(announcementRepository.save(announcement));
    }

    public void delete(Long id) {
        if (!announcementRepository.existsById(id)) {
            throw new AnnouncementNotFoundException("Announcement not found");
        }
        announcementRepository.deleteById(id);
    }

    private Announcement findOrThrow(Long id) {
        return announcementRepository.findById(id)
                .orElseThrow(() -> new AnnouncementNotFoundException("Announcement not found"));
    }
}