package it.gov.pagopa.merchant.mapper;

import it.gov.pagopa.merchant.dto.ReportedUserDTO;
import it.gov.pagopa.merchant.model.ReportedUser;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReportedUserMapper {



    public ReportedUserDTO toDto(ReportedUser entity) {
        return ReportedUserDTO.builder()
                .transactionId(entity.getTransactionId())
                .transactionDate(entity.getTransactionDate())
                .reportedDate(entity.getCreatedAt())
                .build();
    }

    public List<ReportedUserDTO> toDtoList (List<ReportedUser> entities, String fiscalCode){
        return entities.stream()
                .map(e -> {
                    ReportedUserDTO dto = toDto(e);
                    dto.setFiscalCode(fiscalCode);
                    return dto;
                } )
                .toList();
    }
}
