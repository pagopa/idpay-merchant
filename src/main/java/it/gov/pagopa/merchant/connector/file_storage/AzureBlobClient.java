package it.gov.pagopa.merchant.connector.file_storage;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;

@Service
public class AzureBlobClient implements FileStorageConnector {

    private final String merchantFileContainerReference;
    private final CloudBlobClient blobClient;


    AzureBlobClient(@Value("${blobStorage.connectionString}") String storageConnectionString,
            @Value("${blobStorage.merchant.file.containerReference}") String merchantFileContainerReference)
            throws URISyntaxException, InvalidKeyException {
        final CloudStorageAccount storageAccount = CloudStorageAccount.parse(
                storageConnectionString);
        this.blobClient = storageAccount.createCloudBlobClient();
        this.merchantFileContainerReference = merchantFileContainerReference;
    }

    @Override
    public void uploadMerchantFile(InputStream file, String fileName, String contentType) throws URISyntaxException, StorageException, IOException {

        final CloudBlobContainer blobContainer = blobClient.getContainerReference(
                merchantFileContainerReference);
            final CloudBlockBlob blob = blobContainer.getBlockBlobReference(fileName);
            blob.getProperties().setContentType(contentType);
            blob.upload(file, file.available());
    }
    @Override
    public ByteArrayOutputStream downloadMerchantFile(String fileName) throws URISyntaxException, StorageException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final CloudBlobContainer blobContainer = blobClient.getContainerReference(
                merchantFileContainerReference);
        final CloudBlockBlob blob = blobContainer.getBlockBlobReference(fileName);
        blob.download(byteArrayOutputStream);
        return byteArrayOutputStream;
    }
}
