package it.gov.pagopa.merchant.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@Document(collection = "merchant_initiative")
public class MerchantInitiative {
    private String initiativeId;
    private String businessName; //ragione sociale
    private String merchantName; //nome insegna
    private String legalOfficeAddress;
    private String legalOfficeMunicipality; // comune sede legale
    private String legalOfficeProvince;
    private String legalOfficeZipCode;
    private String certifiedEmail;
    private String fiscalCode;
    private String vatNumber;
    private String status;
    private String iban;
    private String acquirerId;
    private LocalDateTime creationDate;
    private LocalDateTime updateDate;
}
