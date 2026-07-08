package it.gov.pagopa.merchant.controller.pdnd;

import it.gov.pagopa.merchant.connector.pdnd.PdndInfoCamereConnectorImpl;
import it.gov.pagopa.merchant.service.pdnd.PdndCacheableService;
import it.gov.pagopa.merchant.utils.DataEncryptionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class PdndInfoCamereConnectorImplTest {

    @Mock
    private PdndCacheableService pdndCacheableService;

    private PdndInfoCamereConnectorImpl pdndInfoCamereConnector;

    @BeforeEach
    void setUp() {
        pdndInfoCamereConnector = new PdndInfoCamereConnectorImpl(pdndCacheableService);
    }

    @Test
    void retrieveAtecoCodes_success() {
        String taxCode = "tax123";
        String encryptedTaxCode = "encryptedTax123";
        List<String> mockAtecoCodes = List.of("1234", "5678");

        mockStatic(DataEncryptionUtils.class);
        when(DataEncryptionUtils.encrypt(taxCode)).thenReturn(encryptedTaxCode);
        when(pdndCacheableService.getAtecoCodes(encryptedTaxCode)).thenReturn(mockAtecoCodes);

        List<String> atecoCodes = pdndInfoCamereConnector.retrieveAtecoCodes(taxCode);

        assertEquals(mockAtecoCodes, atecoCodes);
        verify(pdndCacheableService).getAtecoCodes(encryptedTaxCode);
    }
}