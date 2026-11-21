package com.report.infrastructure.lambda;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.report.application.dto.event.ReportRequestEvent;
import com.report.application.usecase.report.generate.GenerateFeedbackReportUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SqsFeedbackReportHandlerTest {

    @Mock
    private GenerateFeedbackReportUseCase generateFeedbackReportUseCase;

    private SqsFeedbackReportHandler handler;

    @BeforeEach
    void setUp() {
        handler = new SqsFeedbackReportHandler(new ObjectMapper().findAndRegisterModules(), generateFeedbackReportUseCase);
    }

    @Test
    void shouldParseMessageAndInvokeUseCase() {
        SQSEvent.SQSMessage message = new SQSEvent.SQSMessage();
        message.setMessageId("msg-1");
        message.setBody("{\"date\":\"2026-11-20\",\"periodWeek\":\"2026-W47\"}");

        SQSEvent event = new SQSEvent();
        event.setRecords(List.of(message));

        handler.handleRequest(event, null);

        ArgumentCaptor<ReportRequestEvent> eventCaptor = ArgumentCaptor.forClass(ReportRequestEvent.class);
        verify(generateFeedbackReportUseCase).execute(eventCaptor.capture());
        assertEquals(LocalDate.of(2026, 11, 20), eventCaptor.getValue().getDate());
        assertEquals("2026-W47", eventCaptor.getValue().getPeriodWeek());
    }
}
