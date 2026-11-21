package com.report.infrastructure.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.report.application.dto.event.ReportRequestEvent;
import com.report.application.usecase.report.generate.GenerateFeedbackReportUseCase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.RequiredArgsConstructor;
import org.jboss.logging.Logger;

@ApplicationScoped
@Named("sqsFeedbackReportHandler")
@RequiredArgsConstructor(onConstructor_ = { @Inject })
public class SqsFeedbackReportHandler implements RequestHandler<SQSEvent, Void> {
    private static final Logger LOG = Logger.getLogger(SqsFeedbackReportHandler.class);

    private final ObjectMapper objectMapper;
    private final GenerateFeedbackReportUseCase generateFeedbackReportUseCase;

    @Override
    public Void handleRequest(SQSEvent event, Context context) {
        if (event == null || event.getRecords() == null || event.getRecords().isEmpty()) {
            LOG.info("No SQS records to process");
            return null;
        }

        for (SQSEvent.SQSMessage message : event.getRecords()) {
            String messageId = message.getMessageId();
            LOG.infof("Processing report request from SQS messageId=%s", messageId);
            ReportRequestEvent reportRequestEvent = parseMessageBody(message.getBody(), messageId);
            generateFeedbackReportUseCase.execute(reportRequestEvent);
            LOG.infof("Report generated for messageId=%s periodWeek=%s", messageId, reportRequestEvent.getPeriodWeek());
        }
        return null;
    }

    private ReportRequestEvent parseMessageBody(String messageBody, String messageId) {
        try {
            return objectMapper.readValue(messageBody, ReportRequestEvent.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(
                    "Could not parse report request event for messageId=%s".formatted(messageId),
                    e
            );
        }
    }
}
