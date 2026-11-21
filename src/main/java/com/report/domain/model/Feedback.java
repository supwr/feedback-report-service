package com.report.domain.model;

import java.time.Instant;
import java.time.LocalDate;

public record Feedback(
        String id,
        String description,
        Integer rating,
        String urgency,
        Instant createdAt,
        String periodWeek,
        LocalDate sentAt
) {
}
