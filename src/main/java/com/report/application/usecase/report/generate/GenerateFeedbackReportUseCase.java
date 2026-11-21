package com.report.application.usecase.report.generate;

import com.report.application.dto.event.ReportRequestEvent;
import com.report.domain.model.Feedback;
import com.report.domain.port.FeedbackQueryRepository;
import com.report.domain.port.ReportFileStorage;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = { @Inject })
public class GenerateFeedbackReportUseCase {
    private final FeedbackQueryRepository feedbackQueryRepository;
    private final ReportFileStorage reportFileStorage;

    public void execute(ReportRequestEvent requestEvent) {
        validateRequest(requestEvent);

        List<Feedback> feedbacks = feedbackQueryRepository.findByPeriodWeek(requestEvent.getPeriodWeek())
                .stream()
                .sorted(Comparator.comparing(Feedback::createdAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        String csvContent = buildCsv(feedbacks);
        String objectKey = buildObjectKey(requestEvent);
        reportFileStorage.uploadCsv(objectKey, csvContent);
    }

    static String buildObjectKey(ReportRequestEvent requestEvent) {
        return "reports/periodWeek=%s/feedback-report-%s.csv".formatted(
                requestEvent.getPeriodWeek(),
                requestEvent.getDate()
        );
    }

    static String buildCsv(List<Feedback> feedbacks) {
        String header = "id,description,rating,urgency,createdAt,periodWeek,sentAt,evaluationsByDay,evaluationsByUrgency";
        Map<Object, Long> evaluationsByDay = feedbacks.stream()
                .collect(Collectors.groupingBy(Feedback::sentAt, Collectors.counting()));
        Map<Object, Long> evaluationsByUrgency = feedbacks.stream()
                .collect(Collectors.groupingBy(Feedback::urgency, Collectors.counting()));

        String rows = feedbacks.stream()
                .map(feedback -> toCsvLine(feedback, evaluationsByDay, evaluationsByUrgency))
                .collect(Collectors.joining("\n"));
        return rows.isBlank() ? header + "\n" : header + "\n" + rows + "\n";
    }

    private static String toCsvLine(Feedback feedback, Map<Object, Long> evaluationsByDay, Map<Object, Long> evaluationsByUrgency) {
        return String.join(",",
                csvValue(feedback.id()),
                csvValue(feedback.description()),
                csvValue(feedback.rating()),
                csvValue(feedback.urgency()),
                csvValue(feedback.createdAt()),
                csvValue(feedback.periodWeek()),
                csvValue(feedback.sentAt()),
                csvValue(evaluationsByDay.getOrDefault(feedback.sentAt(), 0L)),
                csvValue(evaluationsByUrgency.getOrDefault(feedback.urgency(), 0L))
        );
    }

    private static String csvValue(Object value) {
        if (Objects.isNull(value)) {
            return "\"\"";
        }
        String normalized = String.valueOf(value).replace("\"", "\"\"");
        return "\"" + normalized + "\"";
    }

    private static void validateRequest(ReportRequestEvent requestEvent) {
        if (requestEvent == null) {
            throw new IllegalArgumentException("Event payload is required");
        }
        if (requestEvent.getDate() == null) {
            throw new IllegalArgumentException("Event field date is required");
        }
        if (requestEvent.getPeriodWeek() == null || requestEvent.getPeriodWeek().isBlank()) {
            throw new IllegalArgumentException("Event field periodWeek is required");
        }
    }
}
