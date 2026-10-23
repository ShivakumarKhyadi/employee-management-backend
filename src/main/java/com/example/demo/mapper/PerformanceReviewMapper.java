package com.example.demo.mapper;

import com.example.demo.dto.PerformanceReviewDTO;
import com.example.demo.model.PerformanceReview;

public class PerformanceReviewMapper {

    public static PerformanceReviewDTO toDto(PerformanceReview r){
        return new PerformanceReviewDTO(
          r.getId(),
          r.getRating(), r.getComments(), r.isRecommendAction(), r.getCreatedBy(), r.getCreatedDate()
        );
    }
}
