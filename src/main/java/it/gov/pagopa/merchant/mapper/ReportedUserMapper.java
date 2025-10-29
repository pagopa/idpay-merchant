package it.gov.pagopa.merchant.mapper;

import it.gov.pagopa.merchant.dto.ReportedUserDTO;
import it.gov.pagopa.merchant.dto.ReportedUserRequestDTO;
import it.gov.pagopa.merchant.dto.ReportedUserCreateResponseDTO;
import it.gov.pagopa.merchant.model.ReportedUser;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ReportedUserMapper {

    public ReportedUser fromRequestDtoToEntity(ReportedUserRequestDTO dto) {
        LocalDateTime now = LocalDateTime.now();
        return ReportedUser.builder()
                .merchantId(dto.getMerchantId())
                .userId(dto.getUserFiscalCode())
                .createdAt(now)
                .build();
    }

    public ReportedUserDTO toDto(ReportedUser entity) {
        return ReportedUserDTO.builder()
                .reportedDate(entity.getCreatedAt())
                .build();
    }

    public List<ReportedUserDTO> toDtoList (List<ReportedUser> entities, String fiscalCode){
        return entities.stream()
                .map(e -> new ReportedUserDTO(fiscalCode, e.getCreatedAt()))
                .toList();
    }
}
