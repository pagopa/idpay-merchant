package it.gov.pagopa.merchant.connector.file_storage;

import com.azure.core.http.rest.Response;
import it.gov.pagopa.common.azure.storage.AzureBlobClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import static org.mockito.Mockito.*;

class MerchantBlobClientImplTest {

    private MerchantFileStorageConnector merchantFileStorageConnector;

    @BeforeEach
    void init(){
        merchantFileStorageConnector = spy(new MerchantBlobClientImpl("UseDevelopmentStorage=true;", "test"));
    }

    @Test
    void whenDownloadMerchantFileThenDownloadMethodIsInvoked(){
        // Given
        String filename = "FILENAME";
        ByteArrayOutputStream expectedResult = mock(ByteArrayOutputStream.class);
        doReturn(expectedResult)
                .when((AzureBlobClient)merchantFileStorageConnector)
                        .download(filename);

        // When
        ByteArrayOutputStream result = merchantFileStorageConnector.downloadMerchantFile(filename);

        // Then
        Assertions.assertSame(expectedResult, result);
    }

    @Test
    void whenUploadMerchantFileThenUploadMethodIsInvoked(){
        // Given
        InputStream is = mock(InputStream.class);
        String destination = "FILENAME";
        String contentType = "text";
        doReturn(mock(Response.class))
                .when((AzureBlobClient)merchantFileStorageConnector)
                .upload(is, destination, contentType);

        // When
        merchantFileStorageConnector.uploadMerchantFile(is, destination, contentType);

        // Then
        verify((AzureBlobClient)merchantFileStorageConnector)
                .upload(is, destination, contentType);
    }
}
