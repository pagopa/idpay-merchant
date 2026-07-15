package it.gov.pagopa.merchant.mapper;

import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleInitiativeDTO;
import it.gov.pagopa.merchant.model.Initiative;
import it.gov.pagopa.merchant.model.PointOfSalesInitiative;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.*;

class PointOfSaleInitiativeDTOMapperTest {

    private final PointOfSaleInitiativeDTOMapper mapper = new PointOfSaleInitiativeDTOMapper();

    @Test
    void initiativeEntityToDto_shouldMapAllFields_andForceClosedIfEndDateIsPassed() {
        Initiative initiative = Initiative.builder()
                .initiativeId("INITIATIVE_1")
                .initiativeName("Test Initiative")
                .organizationName("Test Org")
                .startDate(LocalDate.of(2024, Month.JUNE, 1))
                .endDate(LocalDate.of(2024, Month.JUNE, 30))
                .status("ACTIVE")
                .build();

        Instant expectedCreatedAt = Instant.parse("2024-06-15T10:15:30.00Z");
        Instant expectedUpdatedAt = Instant.parse("2024-06-15T12:15:30.00Z");

        PointOfSalesInitiative posInitiative = PointOfSalesInitiative.builder()
                .id("RELATION_1")
                .pointOfSaleId("POS_1")
                .initiativeId("INITIATIVE_1")
                .merchantId("MERCHANT_1")
                .enabled(true)
                .createdAt(expectedCreatedAt)
                .updatedAt(expectedUpdatedAt)
                .build();

        PointOfSaleInitiativeDTO dto = mapper.initiativeEntityToDto(initiative, posInitiative);

        assertNotNull(dto);
        assertEquals("INITIATIVE_1", dto.getInitiativeId());
        assertEquals("Test Initiative", dto.getInitiativeName());
        assertEquals("Test Org", dto.getOrganizationName());
        assertEquals(LocalDate.of(2024, Month.JUNE, 1), dto.getStartDate());
        assertEquals(LocalDate.of(2024, Month.JUNE, 30), dto.getEndDate());
        assertEquals("CLOSED", dto.getStatus());
        assertEquals(expectedCreatedAt, dto.getCreatedAt());
        assertEquals(expectedUpdatedAt, dto.getUpdatedAt());
    }

    @Test
    void initiativeEntityToDto_shouldKeepStatus_ifEndDateIsNull() {
        Initiative initiative = Initiative.builder()
                .initiativeId("INITIATIVE_1")
                .initiativeName("Test Initiative")
                .startDate(LocalDate.now().minusDays(5))
                .endDate(null)
                .status("ACTIVE")
                .build();

        PointOfSaleInitiativeDTO dto = mapper.initiativeEntityToDto(initiative, null);

        assertNotNull(dto);
        assertNull(dto.getEndDate());
        assertEquals("ACTIVE", dto.getStatus());
    }

    @Test
    void initiativeEntityToDto_shouldKeepStatus_ifEndDateIsFuture() {
        Initiative initiative = Initiative.builder()
                .initiativeId("INITIATIVE_1")
                .initiativeName("Test Initiative")
                .startDate(LocalDate.now().minusDays(5))
                .endDate(LocalDate.now().plusDays(10))
                .status("ACTIVE")
                .build();

        PointOfSaleInitiativeDTO dto = mapper.initiativeEntityToDto(initiative, null);

        assertNotNull(dto);
        assertEquals("ACTIVE", dto.getStatus());
    }

    @Test
    void initiativeEntityToDto_shouldMapFieldsEvenIfPosInitiativeIsNull() {
        Initiative initiative = Initiative.builder()
                .initiativeId("INITIATIVE_1")
                .initiativeName("Test Initiative")
                .status("ACTIVE")
                .build();

        PointOfSaleInitiativeDTO dto = mapper.initiativeEntityToDto(initiative, null);

        assertNotNull(dto);
        assertEquals("INITIATIVE_1", dto.getInitiativeId());
        assertNull(dto.getCreatedAt());
        assertNull(dto.getUpdatedAt());
    }

    @Test
    void initiativeEntityToDto_shouldReturnNull_ifInputIsNull() {
        PointOfSaleInitiativeDTO dto = mapper.initiativeEntityToDto(null, null);
        assertNull(dto);
    }
}
