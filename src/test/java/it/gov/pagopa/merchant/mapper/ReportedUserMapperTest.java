package it.gov.pagopa.merchant.mapper;

import it.gov.pagopa.merchant.dto.ReportedUserDTO;
import it.gov.pagopa.merchant.model.ReportedUser;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

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

    @Test
    void toDtoList_shouldMapListAndSetFiscalCode() {
        ReportedUser e1 = ReportedUser.builder()
                .transactionId("T1")
                .trxChargeDate(LocalDateTime.of(2024, 1, 1, 12, 0))
                .createdAt(LocalDateTime.of(2024, 1, 2, 8, 0))
                .build();

        ReportedUser e2 = ReportedUser.builder()
                .transactionId("T2")
                .trxChargeDate(LocalDateTime.of(2024, 2, 1, 12, 0))
                .createdAt(LocalDateTime.of(2024, 2, 2, 8, 0))
                .build();

        String fiscalCode = "ABCDEF12G34H567I";

        List<ReportedUserDTO> result = mapper.toDtoList(List.of(e1, e2), fiscalCode);

        assertNotNull(result);
        assertEquals(2, result.size());

        ReportedUserDTO d1 = result.getFirst();
        assertEquals("T1", d1.getTransactionId());
        assertEquals(e1.getTrxChargeDate(), d1.getTrxChargeDate());
        assertEquals(e1.getCreatedAt(), d1.getReportedDate());
        assertEquals(fiscalCode, d1.getFiscalCode());

        ReportedUserDTO d2 = result.get(1);
        assertEquals("T2", d2.getTransactionId());
        assertEquals(e2.getTrxChargeDate(), d2.getTrxChargeDate());
        assertEquals(e2.getCreatedAt(), d2.getReportedDate());
        assertEquals(fiscalCode, d2.getFiscalCode());
    }

    @Test
    void toDtoList_withEmptyListReturnsEmpty() {
       List<ReportedUserDTO> result = mapper.toDtoList(List.of(), "ANYFC");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
