package com.report.domain.port;

public interface ReportFileStorage {
    void uploadCsv(String objectKey, String csvContent);
}
