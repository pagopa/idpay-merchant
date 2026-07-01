package it.gov.pagopa.merchant.mapper;

import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleInitiativeDTO;
import it.gov.pagopa.merchant.model.Initiative;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PointOfSaleInitiativeDTOMapperTest {

    private final PointOfSaleInitiativeDTOMapper mapper = new PointOfSaleInitiativeDTOMapper();

    @Test
    void initiativeEntityToDto_shouldMapAllFields() {
        Initiative initiative = Initiative.builder()
                .initiativeId("INITIATIVE_1")
                .initiativeName("Test Initiative")
                .organizationName("Test Org")
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2024, 12, 31))
                .status("ACTIVE")
                .build();

        PointOfSaleInitiativeDTO dto = mapper.initiativeEntityToDto(initiative);


        assertNotNull(dto);
        assertEquals("INITIATIVE_1", dto.getInitiativeId());
        assertEquals("Test Initiative", dto.getInitiativeName());
        assertEquals("Test Org", dto.getOrganizationName());
        assertEquals(LocalDate.of(2024, 1, 1), dto.getStartDate());
        assertEquals(LocalDate.of(2024, 12, 31), dto.getEndDate());
        assertEquals("ACTIVE", dto.getStatus());
    }

    @Test
    void initiativeEntityToDto_shouldReturnNull_ifInputIsNull() {
        PointOfSaleInitiativeDTO dto = mapper.initiativeEntityToDto(null);
        assertNull(dto);
    }
}
