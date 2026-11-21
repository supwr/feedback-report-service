package com.report.infrastructure.persistence.repository;

import com.report.domain.model.Feedback;
import com.report.domain.port.FeedbackQueryRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = { @Inject })
public class DynamoDbFeedbackQueryRepository implements FeedbackQueryRepository {
    private final DynamoDbClient dynamoDbClient;

    @ConfigProperty(name = "feedback.table.name")
    String feedbackTableName;

    @ConfigProperty(name = "feedback.table.period-week-index-name")
    String periodWeekIndexName;

    @Override
    public List<Feedback> findByPeriodWeek(String periodWeek) {
        List<Feedback> feedbacks = new ArrayList<>();
        Map<String, AttributeValue> lastEvaluatedKey = null;

        do {
            QueryRequest.Builder requestBuilder = QueryRequest.builder()
                    .tableName(feedbackTableName)
                    .indexName(periodWeekIndexName)
                    .keyConditionExpression("periodWeek = :periodWeek")
                    .expressionAttributeValues(Map.of(
                            ":periodWeek", AttributeValue.fromS(periodWeek)
                    ));

            if (lastEvaluatedKey != null && !lastEvaluatedKey.isEmpty()) {
                requestBuilder.exclusiveStartKey(lastEvaluatedKey);
            }

            QueryResponse response = dynamoDbClient.query(requestBuilder.build());
            for (Map<String, AttributeValue> item : response.items()) {
                feedbacks.add(toDomain(item));
            }
            lastEvaluatedKey = response.lastEvaluatedKey();
        } while (lastEvaluatedKey != null && !lastEvaluatedKey.isEmpty());

        return feedbacks;
    }

    private Feedback toDomain(Map<String, AttributeValue> item) {
        return new Feedback(
                readString(item, "id"),
                readString(item, "description"),
                readInteger(item, "rating"),
                readString(item, "urgency"),
                readInstant(item, "createdAt"),
                readString(item, "periodWeek"),
                readLocalDate(item, "sentAt")
        );
    }

    private static String readString(Map<String, AttributeValue> item, String field) {
        AttributeValue value = item.get(field);
        return value == null ? null : value.s();
    }

    private static Integer readInteger(Map<String, AttributeValue> item, String field) {
        AttributeValue value = item.get(field);
        return value == null ? null : Integer.valueOf(value.n());
    }

    private static Instant readInstant(Map<String, AttributeValue> item, String field) {
        String value = readString(item, field);
        return value == null || value.isBlank() ? null : Instant.parse(value);
    }

    private static LocalDate readLocalDate(Map<String, AttributeValue> item, String field) {
        String value = readString(item, field);
        return value == null || value.isBlank() ? null : LocalDate.parse(value);
    }
}
