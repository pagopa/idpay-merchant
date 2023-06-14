package it.gov.pagopa.merchant.test.fakers;

import it.gov.pagopa.merchant.dto.MerchantUpdateDTO;

import java.time.LocalDateTime;

public class MerchantUpdateDTOFaker {
    private MerchantUpdateDTOFaker() {
    }

    public static MerchantUpdateDTO mockInstance(Integer bias) {
        return mockInstanceBuilder(bias).build();
    }

    public static MerchantUpdateDTO.MerchantUpdateDTOBuilder mockInstanceBuilder(Integer bias) {
        return MerchantUpdateDTO.builder()
                .status("STATUS")
                .errorRow(null)
                .errorKey(null)
                .elabTimeStamp(LocalDateTime.of(2023,5,22,10, 0));


    }
}