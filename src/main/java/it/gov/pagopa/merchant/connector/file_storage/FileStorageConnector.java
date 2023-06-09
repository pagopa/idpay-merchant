package it.gov.pagopa.merchant.connector.file_storage;

import com.microsoft.azure.storage.StorageException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

public interface FileStorageConnector {
    void uploadMerchantFile(InputStream file, String fileName, String contentType) throws URISyntaxException, StorageException, IOException;
    ByteArrayOutputStream downloadMerchantFile(String fileName) throws URISyntaxException, StorageException;
}
