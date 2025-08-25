package it.gov.pagopa.merchant.test.fakers;

import it.gov.pagopa.merchant.dto.MerchantDetailDTO;

import java.time.LocalDateTime;

public class MerchantDetailDTOFaker {

  private MerchantDetailDTOFaker() {
  }

  public static MerchantDetailDTO mockInstance(Integer bias) {
    return mockInstanceBuilder(bias).build();
  }

  public static MerchantDetailDTO.MerchantDetailDTOBuilder mockInstanceBuilder(Integer bias) {
    return MerchantDetailDTO.builder()
            .initiativeId("INITIATIVE_ID_%d".formatted(bias))
            .initiativeName("INITIATIVE_NAME_%d".formatted(bias))
            .businessName("BUSINESS_NAME")
            .legalOfficeAddress("ADDRESS")
            .legalOfficeMunicipality("MUNICIPALITY")
            .legalOfficeProvince("PROVINCE")
            .legalOfficeZipCode("ZIP_CODE")
            .certifiedEmail("MAIL")
            .fiscalCode("FISCAL_CODE")
            .vatNumber("VAT_NUMBER_%d".formatted(bias))
            .status("STATUS")
            .iban("IT60X0542811101000000123455")
            .ibanHolder("Nome Cognome")
            .creationDate(LocalDateTime.of(2023,5,22,10, 0))
            .updateDate(LocalDateTime.of(2023,5,22,10, 0));
  }
}
