package it.gov.pagopa.merchant.mapper;

import it.gov.pagopa.merchant.dto.ReportedUserDTO;
import it.gov.pagopa.merchant.model.ReportedUser;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ReportedUserMapperTest {

    private final ReportedUserMapper mapper = new ReportedUserMapper();


    @Test
    void toDto_shouldMapEntityFields() {
       LocalDateTime trxDate = LocalDateTime.of(2024, 5, 20, 10, 30, 0);
        LocalDateTime createdAt = LocalDateTime.of(2024, 5, 21, 9, 0, 0);

        ReportedUser entity = ReportedUser.builder()
                .transactionId("TRX-001")
                .trxChargeDate(trxDate)
                .createdAt(createdAt)
                .build();

        ReportedUserDTO dto = mapper.toDto(entity);

        assertNotNull(dto);
        assertEquals("TRX-001", dto.getTransactionId());
        assertEquals(trxDate, dto.getTrxChargeDate());
        assertEquals(createdAt, dto.getReportedDate());
    }


}
