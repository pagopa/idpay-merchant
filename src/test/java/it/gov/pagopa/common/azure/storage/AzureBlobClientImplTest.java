package it.gov.pagopa.common.azure.storage;

import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Stubber;
import org.springframework.util.ReflectionUtils;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;


class AzureBlobClientImplTest {

    private AzureBlobClient blobClient;

    @BeforeEach
    void init() {
        blobClient = buildBlobInstance();
    }

    protected AzureBlobClient buildBlobInstance() {
        return new AzureBlobClientImpl("UseDevelopmentStorage=true;", "test");
    }

    @Test
    void testFile() throws IOException {
        // Given
        File testFile = new File("README.md");
        String destination = "baseAzureBlobClientTest/README.md";
        Path downloadPath = Path.of("target/README.md");
        Files.deleteIfExists(downloadPath.toAbsolutePath());

        BlobContainerClient mockClient = mockClientFileOps(testFile, destination, downloadPath);

        // When Upload
        Response<BlockBlobItem> uploadResult = blobClient.uploadFile(testFile, destination, "text");

        // Then uploadResult
        Assertions.assertNotNull(uploadResult);
        Assertions.assertEquals(201, uploadResult.getStatusCode());

        // When List
        List<BlobItem> listResult = blobClient.listFiles(destination).stream().toList();

        // Then listResult
        Assertions.assertNotNull(listResult);
        Assertions.assertEquals(
                List.of("baseAzureBlobClientTest/README.md")
                , listResult.stream().map(BlobItem::getName).toList());

        // When download
        File downloadedFile = downloadPath.toFile();
        Assertions.assertFalse(downloadedFile.exists());
        Response<BlobProperties> downloadResult = blobClient.download(destination, downloadPath);

        // Then downloadResult
        Assertions.assertNotNull(downloadResult);
        Assertions.assertEquals(206, downloadResult.getStatusCode());
        if (mockClient == null) {
            Assertions.assertTrue(downloadedFile.exists());
            Assertions.assertEquals(testFile.length(), downloadedFile.length());
            Assertions.assertTrue(downloadedFile.delete());
        }

        // When Delete
        Response<Boolean> deleteResult = blobClient.deleteFile(destination);

        // Then deleteResult
        Assertions.assertNotNull(deleteResult);
        Assertions.assertTrue(deleteResult.getValue());

        // When List after delete
        if (mockClient != null) {
            mockListFilesOperation(destination, Collections.emptyList(), mockClient);
        }
        List<BlobItem> listAfterDeleteResult = blobClient.listFiles(destination).stream().toList();

        // Then listAfterDeleteResult
        Assertions.assertNotNull(listAfterDeleteResult);
        Assertions.assertEquals(Collections.emptyList(), listAfterDeleteResult);

        // When downloadAfterDeleteResult
        if (mockClient != null) {
            mockDownloadFileOperation(destination, downloadPath, false, mockClient);
        }
        Response<BlobProperties> downloadAfterDeleteResult = blobClient.download(destination, downloadPath);

        // Then downloadResult
        Assertions.assertNull(downloadAfterDeleteResult);
    }

    @Test
    void testStream() throws IOException {
        // Given
        File testFile = new File("README.md");
        String destination = "baseAzureBlobClientTest/README.md";

        try(InputStream inputStream = new BufferedInputStream(new FileInputStream(testFile))) {

            BlobContainerClient mockClient = mockClientStreamOps(inputStream, destination);

            // When Upload
            Response<BlockBlobItem> uploadResult = blobClient.upload(inputStream, destination, "text");

            // Then uploadResult
            Assertions.assertNotNull(uploadResult);
            Assertions.assertEquals(201, uploadResult.getStatusCode());

            // When download
            try (ByteArrayOutputStream downloadedOutputStream = blobClient.download(destination)){
                // Then downloadedOutputStream
                Assertions.assertNotNull(downloadedOutputStream);
                if (mockClient == null) {
                    Assertions.assertEquals(testFile.length(), downloadedOutputStream.size());
                }
            }

            // When Delete
            Response<Boolean> deleteResult = blobClient.deleteFile(destination);

            // Then deleteResult
            Assertions.assertNotNull(deleteResult);
            Assertions.assertTrue(deleteResult.getValue());

            // When downloadAfterDeleteResult
            if (mockClient != null) {
                mockDownloadStreamOperation(destination, false, mockClient);
            }
            try (ByteArrayOutputStream downloadedOutputStream = blobClient.download(destination)){
                // Then downloadedOutputStream
                Assertions.assertNull(downloadedOutputStream);
            }
        }
    }

    protected BlobContainerClient mockClientFileOps(File file, String destination, Path downloadPath) {
        try {
            Field clientField = ReflectionUtils.findField(AzureBlobClientImpl.class, "blobContainerClient");
            Assertions.assertNotNull(clientField);
            clientField.setAccessible(true);

            BlobContainerClient clientMock = mock(BlobContainerClient.class, RETURNS_DEEP_STUBS);

            mockUploadFileOperation(file, destination, clientMock);

            BlobItem mockBlobItem = new BlobItem();
            mockBlobItem.setName(destination);
            mockListFilesOperation(destination, List.of(mockBlobItem), clientMock);

            mockDeleteOperation(destination, clientMock);

            mockDownloadFileOperation(destination, downloadPath, true, clientMock);

            clientField.set(blobClient, clientMock);

            return clientMock;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    protected BlobContainerClient mockClientStreamOps(InputStream inputStream, String destination) {
        try {
            Field clientField = ReflectionUtils.findField(AzureBlobClientImpl.class, "blobContainerClient");
            Assertions.assertNotNull(clientField);
            clientField.setAccessible(true);

            BlobContainerClient clientMock = mock(BlobContainerClient.class, RETURNS_DEEP_STUBS);

            mockUploadStreamOperation(inputStream, destination, clientMock);

            BlobItem mockBlobItem = new BlobItem();
            mockBlobItem.setName(destination);
            mockListFilesOperation(destination, List.of(mockBlobItem), clientMock);

            mockDeleteOperation(destination, clientMock);

            mockDownloadStreamOperation(destination, true, clientMock);

            clientField.set(blobClient, clientMock);

            return clientMock;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static void mockUploadFileOperation(File file, String destination, BlobContainerClient clientMock) {
        @SuppressWarnings("rawtypes") Response responseMock = mock(Response.class);
        when(responseMock.getStatusCode()).thenReturn(201);

        //noinspection unchecked
        when(clientMock.getBlobClient(destination)
                        .uploadFromFileWithResponse(argThat(
                                opt -> file.getPath().equals(opt.getFilePath())
                        ), any(), any()))
                .thenReturn(responseMock);
    }

    private static void mockUploadStreamOperation(InputStream inputStream, String destination, BlobContainerClient clientMock) {
        @SuppressWarnings("rawtypes") Response responseMock = mock(Response.class);
        when(responseMock.getStatusCode()).thenReturn(201);

        //noinspection unchecked
        when(clientMock.getBlobClient(destination)
                        .uploadWithResponse(argThat(
                                opt -> inputStream == opt.getDataStream()
                        ), any(), any()))
                .thenReturn(responseMock);
    }

    private static void mockListFilesOperation(String destination, List<BlobItem> mockedResult, BlobContainerClient clientMock) {
        @SuppressWarnings("rawtypes") PagedIterable responseMock = mock(PagedIterable.class, RETURNS_DEEP_STUBS);
        when(responseMock.stream()).thenReturn(mockedResult.stream());

        //noinspection unchecked
        when(clientMock.listBlobsByHierarchy(destination))
                .thenReturn(responseMock);
    }

    private static void mockDeleteOperation(String destination, BlobContainerClient clientMock) {
        @SuppressWarnings("rawtypes") Response responseMock = mock(Response.class);
        when(responseMock.getValue()).thenReturn(true);

        //noinspection unchecked
        when(clientMock.getBlobClient(destination)
                        .deleteIfExistsWithResponse(eq(DeleteSnapshotsOptionType.INCLUDE), isNull(), any(), any()))
                .thenReturn(responseMock);
    }

    private void mockDownloadFileOperation(String destination, Path downloadPath, boolean fileExists, BlobContainerClient clientMock) {
        BlobClient blobClientMock = clientMock.getBlobClient(destination);

        Stubber stubber;
        if(fileExists) {
            @SuppressWarnings("rawtypes") Response responseMock = mock(Response.class);
            when(responseMock.getStatusCode()).thenReturn(206);
            stubber = doReturn(responseMock);
        } else {
            HttpResponse responseMock = mock(HttpResponse.class);
            when(responseMock.getStatusCode()).thenReturn(404);

            stubber = doThrow(new BlobStorageException("NOT FOUND", responseMock, null));
        }
        stubber
                .when(blobClientMock)
                .downloadToFileWithResponse(argThat(opt ->
                                opt.getFilePath().equals(downloadPath.toString()) && opt.getOpenOptions().equals(Set.of(
                                        StandardOpenOption.CREATE,
                                        StandardOpenOption.TRUNCATE_EXISTING,
                                        StandardOpenOption.READ,
                                        StandardOpenOption.WRITE
                                ))),
                        any(), any());
    }

    private void mockDownloadStreamOperation(String destination, boolean fileExists, BlobContainerClient clientMock) {
        if(!fileExists) {
            HttpResponse responseMock = mock(HttpResponse.class);
            when(responseMock.getStatusCode()).thenReturn(404);

            BlobClient blobClientMock = clientMock.getBlobClient(destination);

            doThrow(new BlobStorageException("NOT FOUND", responseMock, null))
                    .when(blobClientMock)
                    .downloadStream(any());
        }
    }
}
