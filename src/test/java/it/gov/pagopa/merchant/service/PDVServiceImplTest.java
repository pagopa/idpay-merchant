package it.gov.pagopa.merchant.service;

import it.gov.pagopa.merchant.connector.encrypt.EncryptRestConnector;
import it.gov.pagopa.merchant.dto.CFDTO;
import it.gov.pagopa.merchant.exception.custom.PDVInvocationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test aggiornati per PDVServiceImpl:
 * - happy path: ritorna il token dal connector
 * - errore: rilancia PDVInvocationException con messaggio previsto e causa originale
 * - niente dipendenze extra; nessun accesso a getter del CFDTO
 */
@ExtendWith(MockitoExtension.class)
class PDVServiceImplTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private EncryptRestConnector encryptRestConnector;

    @InjectMocks
    private PDVServiceImpl service;

    @Test
    void encryptCF_whenOk_returnsToken() {

        String fiscalCode = "RSSMRA80A01H501U";
        String expectedToken = "enc_123";

        when(encryptRestConnector.upsertToken(any(CFDTO.class)).getToken())
                .thenReturn(expectedToken);

        String out = service.encryptCF(fiscalCode);

        assertEquals(expectedToken, out);
        verify(encryptRestConnector).upsertToken(any(CFDTO.class));
    }


    @Test
    void encryptCF_whenConnectorThrows_wrapsIntoPDVInvocationException() {
        String fiscalCode = "RSSMRA80A01H501U";
        RuntimeException boom = new RuntimeException("boom");

        when(encryptRestConnector.upsertToken(any(CFDTO.class))).thenThrow(boom);

        PDVInvocationException ex = assertThrows(PDVInvocationException.class,
                () -> service.encryptCF(fiscalCode));

        assertEquals("An error occurred during encryption", ex.getMessage());
        assertSame(boom, ex.getCause());
    }

}
