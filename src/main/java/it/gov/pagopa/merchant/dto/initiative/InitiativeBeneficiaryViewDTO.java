package it.gov.pagopa.merchant.dto.initiative;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InitiativeBeneficiaryViewDTO {
    private String initiativeId;
    private String initiativeName;
    private String organizationId;
    private String organizationName;
    private AdditionalInfoDTO additionalInfo;
    private GeneralInfoDTO general;
    private String status;
}
