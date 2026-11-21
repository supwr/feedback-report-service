package com.report.application.usecase.report.generate;

import com.report.application.dto.event.ReportRequestEvent;
import com.report.domain.model.Feedback;
import com.report.domain.port.FeedbackQueryRepository;
import com.report.domain.port.ReportFileStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenerateFeedbackReportUseCaseTest {

    @Mock
    private FeedbackQueryRepository feedbackQueryRepository;

    @Mock
    private ReportFileStorage reportFileStorage;

    private GenerateFeedbackReportUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GenerateFeedbackReportUseCase(feedbackQueryRepository, reportFileStorage);
    }

    @Test
    void shouldGenerateCsvAndUploadToS3() {
        ReportRequestEvent event = ReportRequestEvent.builder()
                .date(LocalDate.of(2026, 11, 20))
                .periodWeek("2026-W47")
                .build();

        when(feedbackQueryRepository.findByPeriodWeek("2026-W47")).thenReturn(List.of(
                new Feedback(
                        "f-1",
                        "feedback with \"quote\"",
                        8,
                        "LOW",
                        Instant.parse("2026-11-20T10:15:30Z"),
                        "2026-W47",
                        LocalDate.of(2026, 11, 20)
                ),
                new Feedback(
                        "f-2",
                        "another",
                        4,
                        "LOW",
                        Instant.parse("2026-11-20T11:15:30Z"),
                        "2026-W47",
                        LocalDate.of(2026, 11, 20)
                ),
                new Feedback(
                        "f-3",
                        "other urgency",
                        2,
                        "HIGH",
                        Instant.parse("2026-11-21T11:15:30Z"),
                        "2026-W47",
                        LocalDate.of(2026, 11, 21)
                )
        ));

        useCase.execute(event);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> csvCaptor = ArgumentCaptor.forClass(String.class);
        verify(reportFileStorage).uploadCsv(keyCaptor.capture(), csvCaptor.capture());

        assertTrue(keyCaptor.getValue().contains("periodWeek=2026-W47"));
        assertTrue(keyCaptor.getValue().endsWith("feedback-report-2026-11-20.csv"));
        assertTrue(csvCaptor.getValue().startsWith("id,description,rating,urgency,createdAt,periodWeek,sentAt,evaluationsByDay,evaluationsByUrgency"));
        assertTrue(csvCaptor.getValue().contains("\"feedback with \"\"quote\"\"\""));
        assertTrue(csvCaptor.getValue().contains("\"f-1\",\"feedback with \"\"quote\"\"\",\"8\",\"LOW\",\"2026-11-20T10:15:30Z\",\"2026-W47\",\"2026-11-20\",\"2\",\"2\""));
        assertTrue(csvCaptor.getValue().contains("\"f-3\",\"other urgency\",\"2\",\"HIGH\",\"2026-11-21T11:15:30Z\",\"2026-W47\",\"2026-11-21\",\"1\",\"1\""));
    }
}
