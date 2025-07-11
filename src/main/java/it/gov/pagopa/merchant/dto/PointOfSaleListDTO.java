package it.gov.pagopa.merchant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointOfSaleListDTO {

    private List<PointOfSaleDTO> content;
    private int pageNo;
    private int pageSize;
    private Long totalElements;
    private Integer totalPages;
}
