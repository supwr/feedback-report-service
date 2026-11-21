package com.report.application.dto.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RegisterForReflection
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReportRequestEvent {
    private LocalDate date;
    private String periodWeek;
}
