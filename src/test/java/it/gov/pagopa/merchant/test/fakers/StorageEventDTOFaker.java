package it.gov.pagopa.merchant.test.fakers;

import it.gov.pagopa.merchant.dto.StorageEventDTO;

public class StorageEventDTOFaker {
    private StorageEventDTOFaker() {
    }

    public static StorageEventDTO mockInstance(Integer bias) {
        return mockInstanceBuilder(bias).build();
    }

    public static StorageEventDTO.StorageEventDTOBuilder mockInstanceBuilder(Integer bias) {
        return StorageEventDTO.builder()
                .subject("/blobServices/containers/refund/merchant/blobs/ORGANIZATIONID%d/INITIATIVEID%d/test.csv".formatted(bias,bias));

    }
}
