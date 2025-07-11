package it.gov.pagopa.merchant.dto.pointofsales;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointOfSalePage {

    private List<PointOfSaleDTO> items;
    private int page;
    private int size;
    private int totalElements;
    private int totalPages;

}
