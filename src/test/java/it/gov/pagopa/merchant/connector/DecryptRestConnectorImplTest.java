package it.gov.pagopa.merchant.connector;

import it.gov.pagopa.merchant.connector.decrypt.DecryptRest;
import it.gov.pagopa.merchant.connector.decrypt.DecryptRestConnectorImpl;
import it.gov.pagopa.merchant.dto.DecryptCfDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DecryptRestConnectorImplTest {

    @Test
    void getPiiByToken_ok_delegatesToClientWithApiKeyAndReturnsResult() {
        // arrange
        String apiKey = "test-api-key";
        String token = "tok-123";
        DecryptRest decryptRest = mock(DecryptRest.class);
        DecryptRestConnectorImpl sut = new DecryptRestConnectorImpl(apiKey, decryptRest);

        DecryptCfDTO expected = new DecryptCfDTO();

        when(decryptRest.getPiiByToken(token, eq(apiKey))).thenReturn(expected);

        DecryptCfDTO res = sut.getPiiByToken(token);

        assertThat(res).isSameAs(expected);
        verify(decryptRest, times(1)).getPiiByToken(token, eq(apiKey));
        verifyNoMoreInteractions(decryptRest);
    }

    @Test
    void getPiiByToken_ko_propagatesExceptionFromClient() {

        String apiKey = "test-api-key";
        String token = "tok-err";
        DecryptRest decryptRest = Mockito.mock(DecryptRest.class);
        DecryptRestConnectorImpl sut = new DecryptRestConnectorImpl(apiKey, decryptRest);

        RuntimeException boom = new RuntimeException("boom");
        when(decryptRest.getPiiByToken((token), eq(apiKey))).thenThrow(boom);

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> sut.getPiiByToken(token));
        assertThat(thrown).isSameAs(boom);
        verify(decryptRest).getPiiByToken(token, eq(apiKey));
        verifyNoMoreInteractions(decryptRest);
    }
}
