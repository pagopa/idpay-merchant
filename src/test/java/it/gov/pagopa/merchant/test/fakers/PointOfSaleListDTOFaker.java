package it.gov.pagopa.merchant.test.fakers;

import it.gov.pagopa.merchant.dto.enums.PointOfSaleTypeEnum;
import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleDTO;
import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleListDTO;

import java.util.List;

public class PointOfSaleListDTOFaker {

    public static PointOfSaleListDTO mockInstance() {
        return mockInstanceBuilder().build();
    }

    public static PointOfSaleListDTO.PointOfSaleListDTOBuilder mockInstanceBuilder() {
        return PointOfSaleListDTO.builder()
                .content(List.of(PointOfSaleDTOFaker.mockInstance()))
                .totalPages(1)
                .totalElements(1L)
                .pageNo(0)
                .pageSize(1);
    }
}
