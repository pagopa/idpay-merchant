package it.gov.pagopa.merchant.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MerchantDetailDTO {

  private String initiativeId;

  private String initiativeName;

  private String businessName; //ragione sociale

  private String legalOfficeAddress;

  private String legalOfficeMunicipality; // comune sede legale

  private String legalOfficeProvince;

  private String legalOfficeZipCode;

  private String certifiedEmail;

  private String fiscalCode;

  private String vatNumber;

  private String status;

  private String iban;

  private String ibanHolder; // intestatario iban

  private Instant creationDate;

  private Instant updateDate;

  private Instant activationDate;
}
