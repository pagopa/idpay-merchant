package it.gov.pagopa.merchant.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportedUserResponseDTO {
    private final String status;
    private final String errorKey;
    private String reportedUserId;
    private String merchantId;
    private String initiativeId;
    private String userId;
    private String motivation;
    private LocalDateTime createdAt;

    public ReportedUserResponseDTO(String status, String errorKey) {
        this.status = status;
        this.errorKey = errorKey;
    }

    public ReportedUserResponseDTO(String status, String errorKey, String reportedUserId) {
        this.status = status;
        this.errorKey = errorKey;
        this.reportedUserId = reportedUserId;
    }

    public ReportedUserResponseDTO(String status, String errorKey, String reportedUserId, String initiativeId, String merchantId, String userId, LocalDateTime createdAt) {
        this.status = status;
        this.errorKey = errorKey;
        this.reportedUserId = reportedUserId;
        this.initiativeId = initiativeId;
        this.merchantId = merchantId;
        this.userId = userId;
        this.createdAt = createdAt;
    }

    public ReportedUserResponseDTO(String status, String errorKey, String reportedUserId, String merchantId, String initiativeId, String userId, String motivation, LocalDateTime createdAt) {
        this.status = status;
        this.errorKey = errorKey;
        this.reportedUserId = reportedUserId;
        this.merchantId = merchantId;
        this.initiativeId = initiativeId;
        this.userId = userId;
        this.motivation = motivation;
        this.createdAt = createdAt;
    }

    public static ReportedUserResponseDTO ok() {
        return new ReportedUserResponseDTO("OK", null);
    }
    public static ReportedUserResponseDTO ok(String reportedUserId, String initiativeId, String merchantId, String userId, LocalDateTime createdAt) {
        return new ReportedUserResponseDTO("OK", null, reportedUserId, initiativeId, merchantId, userId, createdAt);
    }

    public static ReportedUserResponseDTO ko(String errorKey) {
        return new ReportedUserResponseDTO("KO", errorKey);
    }

    public static ReportedUserResponseDTO ko(String errorKey, String reportedUserId) {
        return new ReportedUserResponseDTO("KO", errorKey, reportedUserId);
    }

    public boolean isKo(){
        return this.status.equals("KO");
    }
}

