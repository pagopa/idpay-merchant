package it.gov.pagopa.merchant.mapper;

import it.gov.pagopa.merchant.constants.MerchantConstants;
import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleInitiativeDTO;
import it.gov.pagopa.merchant.model.Initiative;
import it.gov.pagopa.merchant.model.PointOfSalesInitiative;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class PointOfSaleInitiativeDTOMapper {

    public PointOfSaleInitiativeDTO initiativeEntityToDto(Initiative initiative, PointOfSalesInitiative posInitiative){
        if(initiative == null){
            return null;
        }
        String status = initiative.getEndDate() != null && LocalDate.now().isAfter(initiative.getEndDate()) ?
                MerchantConstants.INITIATIVE_CLOSED : initiative.getStatus();

        return PointOfSaleInitiativeDTO.builder()
                .initiativeId(initiative.getInitiativeId())
                .initiativeName(initiative.getInitiativeName())
                .organizationName(initiative.getOrganizationName())
                .startDate(initiative.getStartDate())
                .endDate(initiative.getEndDate())
                .status(status)
                .createdAt(posInitiative != null ? posInitiative.getCreatedAt() : null)
                .updatedAt(posInitiative != null ? posInitiative.getUpdatedAt() : null)
                .onboardingDate(posInitiative != null ? posInitiative.getOnboardingDate() : null)
                .build();
    }

}
