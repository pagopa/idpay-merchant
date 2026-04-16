package it.gov.pagopa.merchant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportedUserDTO {
    private String fiscalCode;
    private Instant reportedDate;
    private String transactionId;
    private Instant trxChargeDate;
}
