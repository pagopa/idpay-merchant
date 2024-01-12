package it.gov.pagopa.merchant.connector.file_storage;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public interface MerchantFileStorageConnector {
    void uploadMerchantFile(InputStream inputStream, String fileName, String contentType);
    ByteArrayOutputStream downloadMerchantFile(String fileName);
}
