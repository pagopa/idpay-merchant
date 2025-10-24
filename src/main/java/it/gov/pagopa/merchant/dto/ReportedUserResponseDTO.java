package it.gov.pagopa.merchant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportedUserResponseDTO {
    String reportedUserId;
    String merchantId;
    String initiativeId;
    String userId;
    String motivation;
    LocalDateTime createdAt;
}

