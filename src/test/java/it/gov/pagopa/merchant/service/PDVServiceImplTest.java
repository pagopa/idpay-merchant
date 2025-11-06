package it.gov.pagopa.merchant.service;

import it.gov.pagopa.merchant.connector.decrypt.DecryptRestConnector;
import it.gov.pagopa.merchant.dto.DecryptCfDTO;
import it.gov.pagopa.merchant.exception.custom.PDVInvocationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class PDVServiceImplTest {

    @Test
    void decryptCF_ok_returnsPiiFromConnector() {

        DecryptRestConnector connector = mock(DecryptRestConnector.class);
        PDVServiceImpl service = new PDVServiceImpl(connector);

        String userId = "enc-user-123";
        String expectedPii = "RSSMRA80A01H501U";

        DecryptCfDTO dto = mock(DecryptCfDTO.class);
        when(connector.getPiiByToken(userId)).thenReturn(dto);
        when(dto.getPii()).thenReturn(expectedPii);


        String result = service.decryptCF(userId);


        assertThat(result).isEqualTo(expectedPii);
        verify(connector, times(1)).getPiiByToken(userId);
        verify(dto, times(1)).getPii();
        verifyNoMoreInteractions(connector, dto);
    }

    @Test
    void decryptCF_ko_wrapsAnyExceptionIntoPDVInvocationException() {

        DecryptRestConnector connector = mock(DecryptRestConnector.class);
        PDVServiceImpl service = new PDVServiceImpl(connector);

        String userId = "enc-user-err";
        RuntimeException boom = new RuntimeException("boom");

        when(connector.getPiiByToken(userId)).thenThrow(boom);


        PDVInvocationException ex = assertThrows(PDVInvocationException.class, () -> service.decryptCF(userId));
        assertThat(ex.getMessage()).contains("An error occurred during decryption");
        assertThat(ex.getCause()).isSameAs(boom);

        verify(connector).getPiiByToken(userId);
        verifyNoMoreInteractions(connector);
    }
}
