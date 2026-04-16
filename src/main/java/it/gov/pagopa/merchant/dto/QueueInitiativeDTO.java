package it.gov.pagopa.merchant.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
@Builder
@NotNull
public class QueueInitiativeDTO {
    private String initiativeId;
    private String status;
    private Instant updateDate;
    private String initiativeRewardType;
}
