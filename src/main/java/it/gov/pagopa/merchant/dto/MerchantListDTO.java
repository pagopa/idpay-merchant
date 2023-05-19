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
public class MerchantListDTO {
    private List<MerchantDTO> content;
    private int pageNo;
    private int pageSize;
    private int totalElements;
    private int totalPages;
}
