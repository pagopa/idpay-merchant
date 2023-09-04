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
import it.gov.pagopa.merchant.event.producer.CommandsProducer;
import it.gov.pagopa.merchant.exception.ClientException;
import it.gov.pagopa.merchant.exception.ClientExceptionWithBody;
import it.gov.pagopa.merchant.model.Merchant;
import it.gov.pagopa.merchant.model.MerchantFile;
import it.gov.pagopa.merchant.repository.MerchantFileRepository;
import it.gov.pagopa.merchant.repository.MerchantRepository;
import it.gov.pagopa.merchant.test.fakers.InitiativeBeneficiaryViewDTOFaker;
import it.gov.pagopa.merchant.test.fakers.MerchantFaker;
import it.gov.pagopa.merchant.test.fakers.MerchantFileFaker;
import it.gov.pagopa.merchant.test.fakers.StorageEventDTOFaker;
import it.gov.pagopa.merchant.utils.AuditUtilities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

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
    private FileStorageConnector fileStorageConnector;
    @Mock
    private CommandsProducer commandsProducer;

    private static final String INITIATIVE_ID = "INITIATIVEID1";
    private static final String ENTITY_ID = "ORGANIZATION_ID";
    private static final String ACQUIRER_ID = "PAGOPA";
    private static final String ORGANIZATION_USER_ID = "ORGANIZATION_USER_ID";
    private static final String FILENAME = "test.csv";
    private static final String PATH_VALID_FILE = "merchantExampleFiles/example_valid.csv";
    private static final String PATH_VALID_FILE_2 = "merchantExampleFiles/example_merchant_valid_iban.csv";

    @BeforeEach
    public void setUp() {
        uploadingMerchantService = new UploadingMerchantServiceImpl(merchantFileRepository, repositoryMock, initiativeRestConnector,
                fileStorageConnector, auditUtilities, commandsProducer);
    }
    @Test
    void uploadMerchantFile_ValidFile() throws IOException {
        File file1 = new ClassPathResource(PATH_VALID_FILE_2).getFile();
        FileInputStream inputStream = new FileInputStream(file1);
        MultipartFile file = new MockMultipartFile("file", FILENAME, "text/csv", inputStream);
        Mockito.when(merchantFileRepository.findByFileNameAndInitiativeId(Mockito.anyString(), Mockito.anyString())).thenReturn(Collections.emptyList());

        MerchantUpdateDTO result = uploadingMerchantService.uploadMerchantFile(file, ENTITY_ID, INITIATIVE_ID, ORGANIZATION_USER_ID, ACQUIRER_ID);

        assertEquals(MerchantConstants.Status.VALIDATED, result.getStatus());
        assertNotNull(result.getElabTimeStamp());
        inputStream.close();
    }

    @ParameterizedTest
    @CsvSource({"empty_file.csv, text/csv, merchant.invalid.file.empty",
            "example_merchant_invalid_format.xml, application/pdf, merchant.invalid.file.format"})
    void uploadMerchantFile_EmptyFile(String path, String content, String error) throws IOException {
        File file1 = new ClassPathResource("merchantExampleFiles" + File.separator + path).getFile();
        FileInputStream inputStream = new FileInputStream(file1);
        MultipartFile file = new MockMultipartFile("file", FILENAME, content, inputStream);

        MerchantUpdateDTO result = uploadingMerchantService.uploadMerchantFile(file, ENTITY_ID, INITIATIVE_ID, ORGANIZATION_USER_ID, ACQUIRER_ID);

        assertEquals(MerchantConstants.Status.KO, result.getStatus());
        assertEquals(error, result.getErrorKey());
        assertNotNull(result.getElabTimeStamp());
        inputStream.close();
    }

    @Test
    void uploadMerchantFile_InvalidFileName() throws IOException {
        MerchantFile merchantFile = MerchantFileFaker.mockInstance(1);
        File file1 = new ClassPathResource(PATH_VALID_FILE_2).getFile();
        FileInputStream inputStream = new FileInputStream(file1);
        MultipartFile file = new MockMultipartFile("file", FILENAME, "text/csv", inputStream);
        Mockito.when(merchantFileRepository.findByFileNameAndInitiativeId(Mockito.anyString(), Mockito.anyString())).thenReturn(List.of(merchantFile));

        MerchantUpdateDTO result = uploadingMerchantService.uploadMerchantFile(file, ENTITY_ID, INITIATIVE_ID, ORGANIZATION_USER_ID, ACQUIRER_ID);

        assertEquals(MerchantConstants.Status.KO, result.getStatus());
        assertEquals(MerchantConstants.Status.KOkeyMessage.INVALID_FILE_NAME, result.getErrorKey());
        assertNotNull(result.getElabTimeStamp());
        inputStream.close();
    }

    @ParameterizedTest
    @CsvSource({"example_merchant_missing_required_fields.csv, merchant.missing.required.fields",
            "example_merchant_invalid_acquirer.csv, merchant.invalid.file.acquirer.wrong",
            "example_merchant_invalid_cf.csv, merchant.invalid.file.cf.wrong",
            "example_merchant_invalid_iban.csv, merchant.invalid.file.iban.wrong",
            "example_merchant_invalid_mail.csv, merchant.invalid.file.email.wrong"})
    void uploadMerchantFile_ReadingFileError(String path, String error) throws IOException {
        File file1 = new ClassPathResource("merchantExampleFiles" + File.separator + path).getFile();
        FileInputStream inputStream = new FileInputStream(file1);
        MultipartFile file = new MockMultipartFile("file", FILENAME, "text/csv", inputStream);

        MerchantUpdateDTO result = uploadingMerchantService.uploadMerchantFile(file, ENTITY_ID, INITIATIVE_ID, ORGANIZATION_USER_ID, ACQUIRER_ID);

        assertEquals(MerchantConstants.Status.KO, result.getStatus());
        assertEquals(error, result.getErrorKey());
        assertNotNull(result.getElabTimeStamp());
    }
    @Test
    void uploadMerchantFile_exception() throws IOException {
        File file1 = new ClassPathResource("merchantExampleFiles/example_invalid.csv").getFile();
        FileInputStream inputStream = new FileInputStream(file1);
        MultipartFile file = new MockMultipartFile("file", FILENAME, "text/csv", inputStream);

        ClientException result = assertThrows(ClientException.class,
                () -> uploadingMerchantService.uploadMerchantFile(file, ENTITY_ID, INITIATIVE_ID, ORGANIZATION_USER_ID, ACQUIRER_ID));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getHttpStatus());
        assertEquals("INTERNAL SERVER ERROR", ((ClientExceptionWithBody) result).getCode());
        assertEquals(String.format(MerchantConstants.CSV_READING_ERROR, INITIATIVE_ID, FILENAME,
                        "Index 17 out of bounds for length 16"), result.getMessage());
        inputStream.close();
    }
    @Test
    void uploadMerchantFile_storeMerchantException() throws URISyntaxException, IOException, StorageException {
        File file1 = new ClassPathResource(PATH_VALID_FILE_2).getFile();
        FileInputStream inputStream = new FileInputStream(file1);
        MultipartFile file = new MockMultipartFile("file", FILENAME, "text/csv", inputStream);

        Mockito.doThrow(new StorageException(null, null, null)).when(fileStorageConnector)
                .uploadMerchantFile(Mockito.any(), Mockito.anyString(), Mockito.anyString());

        ClientException result = assertThrows(ClientException.class,
                () -> uploadingMerchantService.uploadMerchantFile(file, ENTITY_ID, INITIATIVE_ID, ORGANIZATION_USER_ID, ACQUIRER_ID));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getHttpStatus());
        assertEquals("INTERNAL SERVER ERROR", ((ClientExceptionWithBody) result).getCode());
        assertEquals(String.format(MerchantConstants.STORAGE_ERROR, INITIATIVE_ID, FILENAME), result.getMessage());
        inputStream.close();
    }

    @ParameterizedTest
    @ValueSource(strings = {"",
            "/blobServices/containers/merchant/refund/blobs/ORGANIZATIONID1/INITIATIVEID1/test.csv",
            "/blobServices/containers/merchant/refund/blobs/ORGANIZATIONID1/INITIATIVEID1/test.csv",
            "/blobServices/containers/refund/merchant/blobs/ORGANIZATIONID1/INITIATIVEID1/test.csv/test"})
    void ingestionMerchantFile_subjectPathSplitConditionsAndEmptySubject(String subject) {
        StorageEventDTO storageEventDTO = StorageEventDTOFaker.mockInstance(1);
        storageEventDTO.setSubject(subject);
        List<StorageEventDTO> storageEventDTOS = List.of(storageEventDTO);

        try {
            uploadingMerchantService.ingestionMerchantFile(storageEventDTOS);
        } catch (Exception e) {
            fail();
        }
        Mockito.verify(merchantFileRepository, Mockito.times(0))
                .setMerchantFileStatus(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
    }

    @Test
    void ingestionMerchantFile_storageEventDTOEmptyList() {

        try {
            uploadingMerchantService.ingestionMerchantFile(Collections.emptyList());
        } catch (Exception e) {
            fail();
        }
        Mockito.verify(merchantFileRepository, Mockito.times(0))
                .setMerchantFileStatus(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
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
        assertEquals(String.format(MerchantConstants.DOWNLOAD_ERROR, INITIATIVE_ID, FILENAME), result.getMessage());
    }

    @Test
    void ingestionMerchantFile_initiativeException() throws IOException, URISyntaxException, StorageException {
        StorageEventDTO storageEventDTO = StorageEventDTOFaker.mockInstance(1);
        List<StorageEventDTO> storageEventDTOS = List.of(storageEventDTO);

        ClassPathResource resource = new ClassPathResource(PATH_VALID_FILE);
        InputStream inputStream = resource.getInputStream();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        StreamUtils.copy(inputStream, outputStream);
        Mockito.when(fileStorageConnector.downloadMerchantFile(Mockito.anyString())).thenReturn(outputStream);

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
    void ingestionMerchantFile_saveMerchantCheck() throws IOException, URISyntaxException, StorageException {
        InitiativeBeneficiaryViewDTO initiativeBeneficiaryViewDTO = InitiativeBeneficiaryViewDTOFaker.mockInstance(1);
        Mockito.when(initiativeRestConnector.getInitiativeBeneficiaryView(Mockito.anyString())).thenReturn(initiativeBeneficiaryViewDTO);

        StorageEventDTO storageEventDTO = StorageEventDTOFaker.mockInstance(1);
        List<StorageEventDTO> storageEventDTOS = List.of(storageEventDTO);

        ClassPathResource resource = new ClassPathResource(PATH_VALID_FILE_2);
        InputStream inputStream = resource.getInputStream();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        StreamUtils.copy(inputStream, outputStream);
        Mockito.when(fileStorageConnector.downloadMerchantFile(Mockito.anyString())).thenReturn(outputStream);

        Mockito.when(repositoryMock.findByFiscalCodeAndAcquirerId(Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.empty());
        Mockito.when(commandsProducer.sendCommand(Mockito.any())).thenReturn(true);
        try {
            uploadingMerchantService.ingestionMerchantFile(storageEventDTOS);
        } catch (Exception e) {
            fail();
        }
        Mockito.verify(repositoryMock, Mockito.times(3)).findByFiscalCodeAndAcquirerId(Mockito.anyString(),Mockito.anyString());

        inputStream.close();
        outputStream.close();
    }
    @Test
    void ingestionMerchantFile_saveMerchantCheckIban() throws IOException, URISyntaxException, StorageException {
        StorageEventDTO storageEventDTO = StorageEventDTOFaker.mockInstance(1);
        List<StorageEventDTO> storageEventDTOS = List.of(storageEventDTO);

        ClassPathResource resource = new ClassPathResource(PATH_VALID_FILE_2);
        InputStream inputStream = resource.getInputStream();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        StreamUtils.copy(inputStream, outputStream);
        Mockito.when(fileStorageConnector.downloadMerchantFile(Mockito.anyString())).thenReturn(outputStream);

        InitiativeBeneficiaryViewDTO initiativeBeneficiaryViewDTO = InitiativeBeneficiaryViewDTOFaker.mockInstance(1);
        Mockito.when(initiativeRestConnector.getInitiativeBeneficiaryView(Mockito.anyString())).thenReturn(initiativeBeneficiaryViewDTO);

        Merchant merchant = MerchantFaker.mockInstance(1);
        Mockito.when(repositoryMock.findByFiscalCodeAndAcquirerId(Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.of(merchant));
        Mockito.when(commandsProducer.sendCommand(Mockito.any())).thenReturn(false);

        try {
            uploadingMerchantService.ingestionMerchantFile(storageEventDTOS);
        } catch (Exception e) {
            fail();
        }
        Mockito.verify(repositoryMock, Mockito.times(3)).findByFiscalCodeAndAcquirerId(Mockito.anyString(),Mockito.anyString());
        inputStream.close();
        outputStream.close();
    }

    @Test
    void ingestionMerchantFile_saveException() throws IOException, URISyntaxException, StorageException {

        StorageEventDTO storageEventDTO = StorageEventDTOFaker.mockInstance(1);
        List<StorageEventDTO> storageEventDTOS = List.of(storageEventDTO);

        ClassPathResource resource = new ClassPathResource(PATH_VALID_FILE_2);
        InputStream inputStream = resource.getInputStream();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        StreamUtils.copy(inputStream, outputStream);
        Mockito.when(fileStorageConnector.downloadMerchantFile(Mockito.anyString())).thenReturn(outputStream);

        InitiativeBeneficiaryViewDTO initiativeBeneficiaryViewDTO = InitiativeBeneficiaryViewDTOFaker.mockInstance(1);
        Mockito.when(initiativeRestConnector.getInitiativeBeneficiaryView(Mockito.anyString())).thenReturn(initiativeBeneficiaryViewDTO);

        Mockito.when(repositoryMock.findByFiscalCodeAndAcquirerId(Mockito.anyString(),Mockito.anyString())).thenReturn(null);

        ClientException result = assertThrows(ClientException.class,
                () -> uploadingMerchantService.ingestionMerchantFile(storageEventDTOS));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getHttpStatus());
        assertEquals("INTERNAL SERVER ERROR", ((ClientExceptionWithBody) result).getCode());
        assertEquals(String.format(MerchantConstants.MERCHANT_SAVING_ERROR, INITIATIVE_ID, FILENAME), result.getMessage());
        inputStream.close();
        outputStream.close();
    }
}