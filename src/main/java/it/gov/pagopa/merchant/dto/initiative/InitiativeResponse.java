package it.gov.pagopa.merchant.dto.initiative;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InitiativeResponse {
    private String initiativeId;
    private String initiativeName;
    private boolean onboardable;
}