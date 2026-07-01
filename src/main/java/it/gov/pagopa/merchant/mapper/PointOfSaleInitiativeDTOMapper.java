package it.gov.pagopa.merchant.mapper;

import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleInitiativeDTO;
import it.gov.pagopa.merchant.model.Initiative;
import org.springframework.stereotype.Component;

@Component
public class PointOfSaleInitiativeDTOMapper {

    public PointOfSaleInitiativeDTO initiativeEntityToDto(Initiative initiative){
        if(initiative == null){
            return  null;
        }
        return PointOfSaleInitiativeDTO.builder()
                .initiativeId(initiative.getInitiativeId())
                .initiativeName(initiative.getInitiativeName())
                .organizationName(initiative.getOrganizationName())
                .startDate(initiative.getStartDate())
                .endDate(initiative.getEndDate())
                .status(initiative.getStatus())
                .build();
    }

}
