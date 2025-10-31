package it.gov.pagopa.merchant.connector;

import it.gov.pagopa.merchant.connector.encrypt.EncryptRest;
import it.gov.pagopa.merchant.connector.encrypt.EncryptRestConnectorImpl;
import it.gov.pagopa.merchant.dto.CFDTO;
import it.gov.pagopa.merchant.dto.EncryptedCfDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EncryptRestConnectorImplTest {

    @Mock
    private EncryptRest encryptRest;

    @Test
    void upsertToken_delegatesToEncryptRest_andReturnsResponse() {

        String apiKey = "API-KEY-123";
        EncryptRestConnectorImpl connector = new EncryptRestConnectorImpl(apiKey, encryptRest);

        CFDTO input = new CFDTO("RSSMRA80A01H501U");
        EncryptedCfDTO expected = new EncryptedCfDTO("enc_token");
        when(encryptRest.upsertToken(same(input), eq(apiKey))).thenReturn(expected);


        EncryptedCfDTO out = connector.upsertToken(input);


        assertSame(expected, out, "deve restituire esattamente l'oggetto ritornato dal client");
        verify(encryptRest).upsertToken(same(input), eq(apiKey));
        verifyNoMoreInteractions(encryptRest);
    }

    @Test
    void upsertToken_propagatesExceptionsFromClient() {

        String apiKey = "API-KEY-XYZ";
        EncryptRestConnectorImpl connector = new EncryptRestConnectorImpl(apiKey, encryptRest);

        CFDTO input = new CFDTO("RSSMRA80A01H501U");
        RuntimeException boom = new RuntimeException("boom");
        when(encryptRest.upsertToken(same(input), eq(apiKey))).thenThrow(boom);


        RuntimeException ex = assertThrows(RuntimeException.class, () -> connector.upsertToken(input));
        assertSame(boom, ex, "l'eccezione deve essere propagata così com'è");
        verify(encryptRest).upsertToken(same(input), eq(apiKey));
        verifyNoMoreInteractions(encryptRest);
    }

    @Test
    void upsertToken_allowsNullDto_andPassesItThrough() {

        String apiKey = "API-KEY-NULL";
        EncryptRestConnectorImpl connector = new EncryptRestConnectorImpl(apiKey, encryptRest);

        EncryptedCfDTO expected = new EncryptedCfDTO("enc_null");
        when(encryptRest.upsertToken(null, apiKey)).thenReturn(expected);


        EncryptedCfDTO out = connector.upsertToken(null);


        assertSame(expected, out);
        verify(encryptRest).upsertToken(null, apiKey);
        verifyNoMoreInteractions(encryptRest);
    }
}
