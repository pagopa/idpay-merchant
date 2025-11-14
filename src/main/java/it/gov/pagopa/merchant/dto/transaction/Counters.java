package it.gov.pagopa.merchant.dto.transaction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class Counters {
    private Long trxNumber;
    private Long totalRewardCents;
    private Long totalAmountCents;
}
