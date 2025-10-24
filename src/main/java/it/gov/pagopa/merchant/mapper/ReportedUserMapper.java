package it.gov.pagopa.merchant.mapper;

import it.gov.pagopa.merchant.dto.ReportedUserRequestDTO;
import it.gov.pagopa.merchant.dto.ReportedUserResponseDTO;
import it.gov.pagopa.merchant.model.ReportedUser;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ReportedUserMapper {

    public ReportedUser toEntity(ReportedUserRequestDTO dto) {
        LocalDateTime now = LocalDateTime.now();
        return ReportedUser.builder()
                .merchantId(dto.getMerchantId())
                .initiativeId(dto.getInitiativeId())
                .userId(dto.getUserId())
                .createdAt(now)
                .build();
    }

    public ReportedUserResponseDTO toDto(ReportedUser entity) {
        return ReportedUserResponseDTO.builder()
                .reportedUserId(entity.getReportedUserId())
                .merchantId(entity.getMerchantId())
                .initiativeId(entity.getInitiativeId())
                .userId(entity.getUserId())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
