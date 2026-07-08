package it.gov.pagopa.merchant.dto.initiative;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InitiativeResponse {
    private String initiativeId;
    private String initiativeName;
    private String organizationName;
    private String status;
    private String onboardStatus;
    private List<String> atecoCodes;
}