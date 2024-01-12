package it.gov.pagopa.common.azure.storage;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlockBlobItem;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;

public interface AzureBlobClient {
    Response<BlockBlobItem> uploadFile(File file, String destination, String contentType);
    Response<BlockBlobItem> upload(InputStream inputStream, String destination, String contentType);
    Response<Boolean> deleteFile(String destination);
    PagedIterable<BlobItem> listFiles(String path);
    Response<BlobProperties> download(String filePath, Path destination);
    ByteArrayOutputStream download(String filePath);
}
