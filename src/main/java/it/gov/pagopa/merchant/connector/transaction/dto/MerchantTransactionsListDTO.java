package it.gov.pagopa.merchant.connector.transaction.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MerchantTransactionsListDTO {
  private List<MerchantTransactionDTO> content;
  private int pageNo;
  private int pageSize;
  private int totalElements;
  private int totalPages;
}