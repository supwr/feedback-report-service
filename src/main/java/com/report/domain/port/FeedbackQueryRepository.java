package com.report.domain.port;

import com.report.domain.model.Feedback;

import java.util.List;

public interface FeedbackQueryRepository {
    List<Feedback> findByPeriodWeek(String periodWeek);
}
