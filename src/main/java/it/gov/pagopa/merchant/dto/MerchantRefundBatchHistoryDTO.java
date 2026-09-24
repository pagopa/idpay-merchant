package it.gov.pagopa.merchant.dto;

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
public class MerchantRefundBatchHistoryDTO {

  private String merchantId;
  private String fiscalCode;
  private String vatNumber;
  private List<MerchantRefundBatchDTO> rewardBatches;
}

