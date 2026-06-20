package it.gov.pagopa.merchant.dto.initiative;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InitiativeResponse {
    private String initiativeId;
    private String initiativeName;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private String onboardStatus;
}