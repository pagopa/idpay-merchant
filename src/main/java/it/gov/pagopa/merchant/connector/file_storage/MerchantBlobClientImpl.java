package it.gov.pagopa.merchant.connector.file_storage;

import it.gov.pagopa.common.azure.storage.AzureBlobClientImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

@Service
public class MerchantBlobClientImpl extends AzureBlobClientImpl implements MerchantFileStorageConnector {

    MerchantBlobClientImpl(@Value("${blobStorage.connectionString}") String storageConnectionString,
                           @Value("${blobStorage.merchant.file.containerReference}") String merchantFileContainerReference) {
        super(storageConnectionString, merchantFileContainerReference);
    }

    @Override
    public void uploadMerchantFile(InputStream inputStream, String fileName, String contentType) {
        upload(inputStream, fileName, contentType);
    }

    @Override
    public ByteArrayOutputStream downloadMerchantFile(String fileName) {
        return download(fileName);
    }
}
