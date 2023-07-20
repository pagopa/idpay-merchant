package it.gov.pagopa.merchant.mapper;

import it.gov.pagopa.common.utils.TestUtils;
import it.gov.pagopa.merchant.constants.MerchantConstants;
import it.gov.pagopa.merchant.dto.InitiativeDTO;
import it.gov.pagopa.merchant.model.Initiative;
import it.gov.pagopa.merchant.test.fakers.InitiativeFaker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class Initiative2InitiativeDTOMapperTest {

    private Initiative2InitiativeDTOMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new Initiative2InitiativeDTOMapper();
    }

    @Test
    void applyTest() {
        Initiative initiative = InitiativeFaker.mockInstance(1);
        InitiativeDTO initiativeDTO = mapper.apply(initiative);

        assertNotNull(initiativeDTO);
        assertEquals(initiative.getInitiativeId(), initiativeDTO.getInitiativeId());
        assertEquals(initiative.getInitiativeName(), initiativeDTO.getInitiativeName());
        assertEquals(initiative.getOrganizationId(), initiativeDTO.getOrganizationId());
        assertEquals(initiative.getOrganizationName(), initiativeDTO.getOrganizationName());
        assertEquals(initiative.getStartDate(), initiativeDTO.getStartDate());
        assertEquals(initiative.getEndDate(), initiativeDTO.getEndDate());
        assertEquals(initiative.getServiceId(), initiativeDTO.getServiceId());
        assertEquals(initiative.getStatus(), initiativeDTO.getStatus());
        assertEquals(initiative.getMerchantStatus(), initiativeDTO.getMerchantStatus());
        assertEquals(initiative.getCreationDate(), initiativeDTO.getCreationDate());
        assertEquals(initiative.getUpdateDate(), initiativeDTO.getUpdateDate());
        assertEquals(initiative.isEnabled(), initiativeDTO.isEnabled());
        assertAll(() -> {
            assertNotNull(initiativeDTO);
            TestUtils.checkNotNullFields(initiativeDTO);
        });
    }

    @Test
    void applyTest_statusClosed() {
        Initiative initiative = InitiativeFaker.mockInstance(1);
        initiative.setEndDate(LocalDate.now().minusDays(1));
        InitiativeDTO initiativeDTO = mapper.apply(initiative);

        assertNotNull(initiativeDTO);
        assertEquals(MerchantConstants.INITIATIVE_CLOSED, initiativeDTO.getStatus());
        assertAll(() -> {
            assertNotNull(initiativeDTO);
            TestUtils.checkNotNullFields(initiativeDTO);
        });
    }

    @Test
    void applyTest_missingEndDate() {
        Initiative initiative = InitiativeFaker.mockInstance(1);
        initiative.setEndDate(null);
        InitiativeDTO initiativeDTO = mapper.apply(initiative);

        assertNotNull(initiativeDTO);
        assertEquals(MerchantConstants.INITIATIVE_PUBLISHED, initiativeDTO.getStatus());
        assertAll(() -> {
            assertNotNull(initiativeDTO);
            TestUtils.checkNotNullFields(initiativeDTO, "endDate");
        });
    }

}
