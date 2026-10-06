package it.gov.pagopa.merchant.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MerchantRefundBatchDTO {

  private String rewardBatchId;
  private String initiativeId;
  private String month;
  private String status;

  private Long approvedAmountCents;
  private Long suspendedAmountCents;
  private Long initialAmountCents;
  private Long currentAmountCents;
  private Long excludedAmountCents;

  private Long numberOfTransactions;
  private Long numberOfTransactionsSuspended;
  private Long numberOfTransactionsRejected;
  private Long numberOfTransactionsElaborated;
}

