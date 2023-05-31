package it.gov.pagopa.merchant.mapper;

import it.gov.pagopa.merchant.constants.MerchantConstants;
import it.gov.pagopa.merchant.dto.InitiativeDTO;
import it.gov.pagopa.merchant.model.Initiative;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class Initiative2InitiativeDTOMapper {

    public InitiativeDTO apply(Initiative initiative) {
        String status = initiative.getEndDate() != null && LocalDate.now().isAfter(initiative.getEndDate()) ?
                MerchantConstants.INITIATIVE_CLOSED : initiative.getStatus();
        return InitiativeDTO.builder()
                .initiativeId(initiative.getInitiativeId())
                .initiativeName(initiative.getInitiativeName())
                .organizationId(initiative.getOrganizationId())
                .organizationName(initiative.getOrganizationName())
                .serviceId(initiative.getServiceId())
                .startDate(initiative.getStartDate())
                .endDate(initiative.getEndDate())
                .status(status)
                .merchantStatus(initiative.getMerchantStatus())
                .creationDate(initiative.getCreationDate())
                .updateDate(initiative.getUpdateDate())
                .enabled(initiative.isEnabled())
                .build();
    }
}
