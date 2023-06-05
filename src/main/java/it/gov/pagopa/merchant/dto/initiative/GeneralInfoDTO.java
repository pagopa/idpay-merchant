package it.gov.pagopa.merchant.dto.initiative;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
@Builder
@Data
public class GeneralInfoDTO {

    private LocalDate startDate;
    private LocalDate endDate;
}
