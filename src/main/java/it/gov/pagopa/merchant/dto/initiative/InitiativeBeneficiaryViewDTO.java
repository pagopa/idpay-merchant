package it.gov.pagopa.merchant.dto.initiative;

import lombok.Data;

import java.time.LocalDate;

@Data
public class InitiativeBeneficiaryViewDTO {
    private String initiativeId;
    private String initiativeName;
    private String organizationId;
    private String organizationName;
    private AdditionalInfoDTO additionalInfo;
    private GeneralInfoDTO general;
    private String status;
}
