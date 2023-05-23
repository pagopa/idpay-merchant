package it.gov.pagopa.merchant.mapper;

import it.gov.pagopa.merchant.dto.InitiativeDTO;
import it.gov.pagopa.merchant.mapper.Initiative2InitiativeDTOMapper;
import it.gov.pagopa.merchant.model.Initiative;
import it.gov.pagopa.merchant.test.fakers.InitiativeFaker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
        assertEquals(initiative.getMerchantStatus(), initiativeDTO.getMerchantStatus());
        assertEquals(initiative.getCreationDate(), initiativeDTO.getCreationDate());
        assertEquals(initiative.getUpdateDate(), initiativeDTO.getUpdateDate());
        assertEquals(initiative.isEnabled(), initiativeDTO.isEnabled());
    }
}
