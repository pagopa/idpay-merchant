package it.gov.pagopa.merchant.mapper;

import it.gov.pagopa.merchant.dto.InitiativeDTO;
import it.gov.pagopa.merchant.model.Initiative;
import org.springframework.stereotype.Service;

@Service
public class Initiative2InitiativeDTOMapper {

    public InitiativeDTO apply(Initiative initiative) {
        return InitiativeDTO.builder()
                .initiativeId(initiative.getInitiativeId())
                .initiativeName(initiative.getInitiativeName())
                .merchantStatus(initiative.getMerchantStatus())
                .creationDate(initiative.getCreationDate())
                .updateDate(initiative.getUpdateDate())
                .enabled(initiative.isEnabled())
                .build();
    }
}
