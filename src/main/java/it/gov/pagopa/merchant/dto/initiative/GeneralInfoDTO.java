package it.gov.pagopa.merchant.dto.initiative;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GeneralInfoDTO {

    private Instant startDate;
    private Instant endDate;
}
