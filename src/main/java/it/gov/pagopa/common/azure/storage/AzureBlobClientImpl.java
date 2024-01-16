package it.gov.pagopa.common.azure.storage;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.*;
import com.azure.storage.blob.options.BlobDownloadToFileOptions;
import com.azure.storage.blob.options.BlobParallelUploadOptions;
import com.azure.storage.blob.options.BlobUploadFromFileOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Set;

@Slf4j
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class AzureBlobClientImpl implements AzureBlobClient {

    private final BlobContainerClient blobContainerClient;

    protected AzureBlobClientImpl(
            String storageConnectionString,
            String blobContainerName
    ) {
        this.blobContainerClient = new BlobServiceClientBuilder()
                .connectionString(storageConnectionString)
                .buildClient()
                .getBlobContainerClient(blobContainerName);
    }

    @Override
    public Response<BlockBlobItem> uploadFile(File file, String destination, String contentType) {
        log.info("Uploading file {} (contentType={}) into azure blob at destination {}", file.getName(), contentType, destination);

        return blobContainerClient.getBlobClient(destination)
                .uploadFromFileWithResponse(new BlobUploadFromFileOptions(file.getPath()), null, null);
    }

    @Override
    public Response<BlockBlobItem> upload(InputStream inputStream, String destination, String contentType) {
        log.info("Uploading (contentType={}) into azure blob at destination {}", contentType, destination);

        return blobContainerClient.getBlobClient(destination)
                .uploadWithResponse(new BlobParallelUploadOptions(inputStream), null, null);
    }

    @Override
    public Response<Boolean> deleteFile(String destination) {
        log.info("Deleting file {} from azure blob container", destination);

        return blobContainerClient.getBlobClient(destination)
                .deleteIfExistsWithResponse(DeleteSnapshotsOptionType.INCLUDE, null, null, null);
    }

    @Override
    public PagedIterable<BlobItem> listFiles(String path) {
        return blobContainerClient.listBlobsByHierarchy(path);
    }

    @Override
    public Response<BlobProperties> download(String filePath, Path destination) {
        log.info("Downloading file {} from azure blob container", filePath);

        createDirectoryIfNotExists(destination);

        try {
            return blobContainerClient.getBlobClient(filePath)
                    .downloadToFileWithResponse(new BlobDownloadToFileOptions(destination.toString())
                                    // override options
                                    .setOpenOptions(Set.of(
                                            StandardOpenOption.CREATE,
                                            StandardOpenOption.TRUNCATE_EXISTING,
                                            StandardOpenOption.READ,
                                            StandardOpenOption.WRITE)),
                            null, null
                    );
        } catch (BlobStorageException e) {
            if(e.getStatusCode()!=404){
                throw e;
            } else {
                return null;
            }
        }
    }

    @Override
    public ByteArrayOutputStream download(String filePath) {
        log.info("Downloading file {} from azure blob container", filePath);

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            blobContainerClient.getBlobClient(filePath)
                    .downloadStream(outputStream);
            return outputStream;
        } catch (BlobStorageException e) {
            if(e.getStatusCode()!=404){
                throw e;
            } else {
                return null;
            }
        }
    }

    private static void createDirectoryIfNotExists(Path localFile) {
        Path directory = localFile.getParent();
        if (!Files.exists(directory)) {
            try {
                Files.createDirectories(directory);
            } catch (IOException e) {
                throw new IllegalStateException("Cannot create directory to store downloaded zip %s".formatted(localFile), e);
            }
        }
    }
}
