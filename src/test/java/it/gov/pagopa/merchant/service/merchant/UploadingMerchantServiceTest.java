package it.gov.pagopa.merchant.service.merchant;

import com.microsoft.azure.storage.StorageException;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import it.gov.pagopa.merchant.connector.file_storage.FileStorageConnector;
import it.gov.pagopa.merchant.connector.initiative.InitiativeRestConnector;
import it.gov.pagopa.merchant.constants.MerchantConstants;
import it.gov.pagopa.merchant.dto.MerchantUpdateDTO;
import it.gov.pagopa.merchant.dto.StorageEventDTO;
import it.gov.pagopa.merchant.dto.initiative.InitiativeBeneficiaryViewDTO;
import it.gov.pagopa.merchant.exception.ClientException;
import it.gov.pagopa.merchant.exception.ClientExceptionWithBody;
import it.gov.pagopa.merchant.model.MerchantFile;
import it.gov.pagopa.merchant.repository.MerchantFileRepository;
import it.gov.pagopa.merchant.repository.MerchantRepository;
import it.gov.pagopa.merchant.test.fakers.InitiativeBeneficiaryViewDTOFaker;
import it.gov.pagopa.merchant.test.fakers.MerchantFileFaker;
import it.gov.pagopa.merchant.test.fakers.StorageEventDTOFaker;
import it.gov.pagopa.merchant.test.utils.TestUtils;
import it.gov.pagopa.merchant.utils.AuditUtilities;
import it.gov.pagopa.merchant.utils.Utilities;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class UploadingMerchantServiceTest {
    UploadingMerchantService uploadingMerchantService;
    @Mock
    private MerchantRepository repositoryMock;
    @Mock
    private MerchantFileRepository merchantFileRepository;
    @Mock
    private InitiativeRestConnector initiativeRestConnector;
    @Mock
    private AuditUtilities auditUtilities;
    @Mock
    private Utilities utilities;
    @Mock
    private FileStorageConnector fileStorageConnector;
    @Mock
    private BufferedReader bufferedReader;
    @Mock
    private InputStreamReader mockStream;
    private final String INITIATIVE_ID = "INITIATIVE_ID";
    private static final String ORGANIZATION_ID = "ORGANIZATION_ID";
    private final String ORGANIZATION_USER_ID = "ORGANIZATION_USER_ID";
    private static final String FILENAME = "filename";
    private final Path sampleCsv = Path.of("target/tmp/merchantExampleFiles/example_merchant_valid.csv");

    @BeforeEach
    public void setUp() {
        uploadingMerchantService = new UploadingMerchantServiceImpl(merchantFileRepository, repositoryMock, initiativeRestConnector,
                fileStorageConnector, utilities, auditUtilities);
    }

    @AfterEach
    void checkCsvExistance() throws IOException {
        try {
            TestUtils.waitFor(() -> !Files.exists(sampleCsv), () -> "The local csv has not been deleted! %s".formatted(sampleCsv), 5, 500);
        } finally {
            Files.deleteIfExists(sampleCsv);
            Files.deleteIfExists(sampleCsv.getParent());
        }
    }

    @Test
    void uploadMerchantFile_ValidFile() throws IOException {
        File file1 = new ClassPathResource("merchantExampleFiles" + File.separator + "example_merchant_valid_iban_different.csv").getFile();
        FileInputStream inputStream = new FileInputStream(file1);
        MultipartFile file = new MockMultipartFile("file", FILENAME, "text/csv", inputStream);
        Mockito.when(merchantFileRepository.findByFileNameAndInitiativeId(Mockito.anyString(), Mockito.anyString())).thenReturn(Collections.emptyList());

        MerchantUpdateDTO result = uploadingMerchantService.uploadMerchantFile(file, ORGANIZATION_ID, INITIATIVE_ID, ORGANIZATION_USER_ID);

        Assertions.assertEquals("VALIDATED", result.getStatus());
        Assertions.assertNotNull(result.getElabTimeStamp());
    }

    @ParameterizedTest
    @CsvSource({"empty_file.csv, text/csv, merchant.invalid.file.empty",
            "example_merchant_invalid_format.xml, application/pdf, merchant.invalid.file.format"})
    void uploadMerchantFile_EmptyFile(String path, String content, String error) throws IOException {
        File file1 = new ClassPathResource("merchantExampleFiles" + File.separator + path).getFile();
        FileInputStream inputStream = new FileInputStream(file1);
        MultipartFile file = new MockMultipartFile("file", FILENAME, content, inputStream);

        MerchantUpdateDTO result = uploadingMerchantService.uploadMerchantFile(file, ORGANIZATION_ID, INITIATIVE_ID, ORGANIZATION_USER_ID);

        Assertions.assertEquals("KO", result.getStatus());
        Assertions.assertEquals(error, result.getErrorKey());
        Assertions.assertNotNull(result.getElabTimeStamp());
    }

    @Test
    void uploadMerchantFile_InvalidFileName() throws IOException {
        MerchantFile merchantFile = MerchantFileFaker.mockInstance(1);
        File file1 = new ClassPathResource("merchantExampleFiles" + File.separator + "example_merchant_valid_iban_different.csv").getFile();
        FileInputStream inputStream = new FileInputStream(file1);
        MultipartFile file = new MockMultipartFile("file", FILENAME, "text/csv", inputStream);
        Mockito.when(merchantFileRepository.findByFileNameAndInitiativeId(Mockito.anyString(), Mockito.anyString())).thenReturn(List.of(merchantFile));

        MerchantUpdateDTO result = uploadingMerchantService.uploadMerchantFile(file, ORGANIZATION_ID, INITIATIVE_ID, ORGANIZATION_USER_ID);

        Assertions.assertEquals("KO", result.getStatus());
        Assertions.assertEquals("merchant.invalid.file.name", result.getErrorKey());
        Assertions.assertNotNull(result.getElabTimeStamp());
    }

    @ParameterizedTest
    @CsvSource({"example_merchant_missing_required_fields.csv, merchant.missing.required.fields",
            "example_merchant_invalid_cf.csv, merchant.invalid.file.cf.wrong",
            "example_merchant_invalid_iban.csv, merchant.invalid.file.iban.wrong",
            "example_merchant_invalid _mail.csv, merchant.invalid.file.email.wrong"})
    void uploadMerchantFile_ReadingFileError(String path, String error) throws IOException {
        File file1 = new ClassPathResource("merchantExampleFiles" + File.separator + path).getFile();
        FileInputStream inputStream = new FileInputStream(file1);
        MultipartFile file = new MockMultipartFile("file", FILENAME, "text/csv", inputStream);

        MerchantUpdateDTO result = uploadingMerchantService.uploadMerchantFile(file, ORGANIZATION_ID, INITIATIVE_ID, ORGANIZATION_USER_ID);

        Assertions.assertEquals("KO", result.getStatus());
        Assertions.assertEquals(error, result.getErrorKey());
        Assertions.assertNotNull(result.getElabTimeStamp());
    }

    /*@Test
    void updateMerchantFile_errorFileReading() throws Exception {
        Mockito.when(merchantFileRepository.findByFileNameAndInitiativeId(Mockito.anyString(), Mockito.anyString())).thenReturn(Collections.emptyList());

        File file1 = new ClassPathResource("merchantExampleFiles" + File.separator + "example_merchant_valid.csv").getFile();
        FileInputStream inputStream = new FileInputStream(file1);
        MultipartFile file = new MockMultipartFile("file", FILENAME, "text/csv", inputStream);

        Mockito.when(bufferedReader.readLine()).thenThrow(IOException.class);

        try {
            uploadingMerchantService.uploadMerchantFile(file,ORGANIZATION_ID, INITIATIVE_ID, "ORGANIZATION_USER_ID");
        } catch (ClientExceptionWithBody e) {
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getHttpStatus());
            assertEquals(MerchantConstants.INTERNAL_SERVER_ERROR, e.getCode());
            assertEquals(String.format(MerchantConstants.CSV_READING_ERROR,INITIATIVE_ID,FILENAME,
                    e.getMessage()), e.getMessage());
        }
    }
     */
    @Test
    void ingestionMerchantFile_InitiativeNotPublished() throws URISyntaxException, StorageException, IOException {
        InitiativeBeneficiaryViewDTO initiativeBeneficiaryViewDTO = InitiativeBeneficiaryViewDTOFaker.mockInstance(1);
        initiativeBeneficiaryViewDTO.setStatus("DRAFT");
        Mockito.when(initiativeRestConnector.getInitiativeBeneficiaryView(Mockito.anyString())).thenReturn(initiativeBeneficiaryViewDTO);

        StorageEventDTO storageEventDTO = StorageEventDTOFaker.mockInstance(1);
        List<StorageEventDTO> storageEventDTOS = List.of(storageEventDTO);

        File file1 = new ClassPathResource("merchantExampleFiles" + File.separator + "example_merchant_valid.csv").getFile();
        FileOutputStream outputStream = new FileOutputStream(file1);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.writeTo(outputStream);

        Mockito.when(fileStorageConnector.downloadMerchantFile(Mockito.anyString())).thenReturn(byteArrayOutputStream);

        ClientException result = assertThrows(ClientException.class,
                () -> uploadingMerchantService.ingestionMerchantFile(storageEventDTOS));
        assertEquals(HttpStatus.FORBIDDEN, result.getHttpStatus());
        assertEquals("FORBIDDEN", ((ClientExceptionWithBody) result).getCode());
        assertEquals(String.format("Initiative %s not published",
                "INITIATIVEID1"), result.getMessage());
    }
    @Test
    void ingestionMerchantFile_initiativeException() throws IOException, URISyntaxException, StorageException {

        StorageEventDTO storageEventDTO = StorageEventDTOFaker.mockInstance(1);
        List<StorageEventDTO> storageEventDTOS = List.of(storageEventDTO);

        File file1 = new ClassPathResource("merchantExampleFiles" + File.separator + "example_merchant_valid.csv").getFile();
        FileOutputStream outputStream = new FileOutputStream(file1);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.writeTo(outputStream);

        Mockito.when(fileStorageConnector.downloadMerchantFile(Mockito.anyString())).thenReturn(byteArrayOutputStream);

        Request request =
                Request.create(Request.HttpMethod.PUT, "url", new HashMap<>(), null, new RequestTemplate());

        Mockito.doThrow(new FeignException.BadRequest("", request, new byte[0], null))
                .when(initiativeRestConnector)
                .getInitiativeBeneficiaryView(Mockito.anyString());

        ClientException result = assertThrows(ClientException.class,
                () -> uploadingMerchantService.ingestionMerchantFile(storageEventDTOS));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getHttpStatus());
        assertEquals("Something went wrong", result.getMessage());
    }
    @Test
    void ingestionMerchantFile_downloadException() throws URISyntaxException, StorageException {

        StorageEventDTO storageEventDTO = StorageEventDTOFaker.mockInstance(1);
        List<StorageEventDTO> storageEventDTOS = List.of(storageEventDTO);

        Mockito.when(fileStorageConnector.downloadMerchantFile(Mockito.anyString())).thenThrow(new StorageException(null, null, null));

        ClientException result = assertThrows(ClientException.class,
                () -> uploadingMerchantService.ingestionMerchantFile(storageEventDTOS));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getHttpStatus());
        assertEquals("INTERNAL SERVER ERROR", ((ClientExceptionWithBody) result).getCode());
        assertEquals(String.format(MerchantConstants.DOWNLOAD_ERROR, "INITIATIVEID1", "test.csv"), result.getMessage());
    }

   /* @Test
    void uploadMerchantFile_saveInvalidFileKo() {
        InitiativeBeneficiaryViewDTO initiativeBeneficiaryViewDTO = InitiativeBeneficiaryViewDTOFaker.mockInstance(1);
        Mockito.when(initiativeRestConnector.getInitiativeBeneficiaryView(Mockito.anyString())).thenReturn(initiativeBeneficiaryViewDTO);
        Mockito.doThrow(new ClientExceptionWithBody(HttpStatus.INTERNAL_SERVER_ERROR, "", "")).when(repositoryMock).save(any());
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try {
            uploadingMerchantService.saveMerchants(byteArrayOutputStream, FILENAME, ORGANIZATION_ID, INITIATIVE_ID);
        } catch (ClientExceptionWithBody e) {
            Assertions.assertEquals("Initiative %s - file %s: error during merchant saving", e.getMessage());
            Assertions.assertEquals("INTERNAL_SERVER_ERROR", e.getCode());
        }
    }

    */
}