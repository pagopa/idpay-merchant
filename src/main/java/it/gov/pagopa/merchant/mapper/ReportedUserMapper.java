package it.gov.pagopa.merchant.mapper;

import it.gov.pagopa.merchant.dto.ReportedUserDTO;
import it.gov.pagopa.merchant.model.ReportedUser;
import org.springframework.stereotype.Component;


@Component
public class ReportedUserMapper {

    public ReportedUserDTO toDto(ReportedUser entity) {
        return ReportedUserDTO.builder()
                .transactionId(entity.getTransactionId())
                .trxChargeDate(entity.getTrxChargeDate())
                .reportedDate(entity.getCreatedAt())
                .build();
    }

}
