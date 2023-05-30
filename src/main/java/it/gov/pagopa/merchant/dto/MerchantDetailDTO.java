package it.gov.pagopa.merchant.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class MerchantDetailDTO extends MerchantDetailBaseDTO {
    private String initiativeId;
    private String businessName; //ragione sociale
    private String legalOfficeAddress;
    private String legalOfficeMunicipality; // comune sede legale
    private String legalOfficeProvince;
    private String legalOfficeZipCode;
    private String certifiedEmail;
    private String status;
    private String iban;
    private LocalDateTime creationDate;
    private LocalDateTime updateDate;

}
