package it.gov.pagopa.merchant.dto.initiative;

import lombok.Data;

import java.time.LocalDate;
@Data
public class GeneralInfoDTO {

    private LocalDate startDate;
    private LocalDate endDate;
}
