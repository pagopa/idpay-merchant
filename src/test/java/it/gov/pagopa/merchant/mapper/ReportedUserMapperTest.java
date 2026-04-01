package it.gov.pagopa.merchant.mapper;

import it.gov.pagopa.merchant.dto.ReportedUserDTO;
import it.gov.pagopa.merchant.model.ReportedUser;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ReportedUserMapperTest {

    private final ReportedUserMapper mapper = new ReportedUserMapper();


    @Test
    void toDto_shouldMapEntityFields() {

        ZoneId zone = ZoneId.systemDefault();

        Instant trxDate = LocalDateTime.of(2024, 5, 20, 10, 30, 0)
                .atZone(zone)
                .toInstant();

        Instant createdAt = LocalDateTime.of(2024, 5, 21, 9, 0, 0)
                .atZone(zone)
                .toInstant();


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

        Instant now = Instant.now();
        ReportedUser e1 = ReportedUser.builder()
                .transactionId("trx-1")
                .trxChargeDate(now.minus(2, ChronoUnit.DAYS))
                .createdAt(now.minus(1,ChronoUnit.DAYS))
                .build();

        ReportedUser e2 = ReportedUser.builder()
                .transactionId("trx-2")
                .trxChargeDate(now.minus(5,ChronoUnit.DAYS))
                .createdAt(now.minus(4,ChronoUnit.DAYS))
                .build();

        List<ReportedUser> entities = List.of(e1, e2);
        String fiscalCode = "ABCDEF12G34H567I";


        List<ReportedUserDTO> dtos = mapper.toDtoList(entities, fiscalCode);


        assertThat(dtos).hasSize(2);
        assertThat(dtos)
                .extracting(ReportedUserDTO::getFiscalCode)
                .containsOnly(fiscalCode);


        ReportedUserDTO dto1 = dtos.getFirst();
        assertThat(dto1.getTransactionId()).isEqualTo(e1.getTransactionId());
        assertThat(dto1.getTrxChargeDate()).isEqualTo(e1.getTrxChargeDate());
        assertThat(dto1.getReportedDate()).isEqualTo(e1.getCreatedAt());


        ReportedUserDTO dto2 = dtos.get(1);
        assertThat(dto2.getTransactionId()).isEqualTo(e2.getTransactionId());
        assertThat(dto2.getTrxChargeDate()).isEqualTo(e2.getTrxChargeDate());
        assertThat(dto2.getReportedDate()).isEqualTo(e2.getCreatedAt());
    }

}
