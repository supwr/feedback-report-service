package com.report.infrastructure.storage;

import com.report.domain.port.ReportFileStorage;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.nio.charset.StandardCharsets;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = { @Inject })
public class S3ReportFileStorage implements ReportFileStorage {
    private final S3Client s3Client;

    @ConfigProperty(name = "feedback.report.bucket.name")
    String feedbackReportBucketName;

    @Override
    public void uploadCsv(String objectKey, String csvContent) {
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(feedbackReportBucketName)
                        .key(objectKey)
                        .contentType("text/csv")
                        .build(),
                RequestBody.fromString(csvContent, StandardCharsets.UTF_8)
        );
    }
}
