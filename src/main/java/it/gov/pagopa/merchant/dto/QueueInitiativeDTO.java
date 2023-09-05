package it.gov.pagopa.merchant.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
@NotNull
public class QueueInitiativeDTO {
    private String initiativeId;
    private String status;
    private LocalDateTime updateDate;
    private String initiativeRewardType;
}
