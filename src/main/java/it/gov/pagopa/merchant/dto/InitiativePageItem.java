package it.gov.pagopa.merchant.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitiativePageItem {

    private String initiativeId;
    private String initiativeName;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private String onboardStatus;
    private Integer onboardStatusOrder;
}