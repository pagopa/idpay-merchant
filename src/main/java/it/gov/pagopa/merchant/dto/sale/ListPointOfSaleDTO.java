package it.gov.pagopa.merchant.dto.sale;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListPointOfSaleDTO {

    private List<PointOfSaleDTO> pointOfSale;
    private int pageNumber;
    private int pageSize;
    private int totalElements;
    private int totalPages;

}
