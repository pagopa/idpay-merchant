package it.gov.pagopa.merchant.model;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@Document(collection = "merchant")
public class Merchant {
    @MongoId
    private String merchantId;
    private String acquirerId;
    private String businessName; //ragione sociale
    private String legalOfficeAddress;
    private String legalOfficeMunicipality; // comune sede legale
    private String legalOfficeProvince;
    private String legalOfficeZipCode;
    private String certifiedEmail;
    private String fiscalCode;
    private String vatNumber;
    private String iban;
    private String ibanHolder; // intestatario iban
    private List<Initiative> initiativeList;
    private boolean enabled;
    private LocalDate activationDate;
}
