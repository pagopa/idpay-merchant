package it.gov.pagopa.merchant.service.merchant;

import it.gov.pagopa.merchant.connector.file_storage.FileStorageConnector;
import it.gov.pagopa.merchant.connector.initiative.InitiativeRestConnector;
import it.gov.pagopa.merchant.constants.MerchantConstants;
import it.gov.pagopa.merchant.dto.MerchantUpdateDTO;
import it.gov.pagopa.merchant.dto.StorageEventDTO;
import it.gov.pagopa.merchant.exception.ClientExceptionNoBody;
import it.gov.pagopa.merchant.exception.ClientExceptionWithBody;
import it.gov.pagopa.merchant.model.Initiative;
import it.gov.pagopa.merchant.model.Merchant;
import it.gov.pagopa.merchant.model.MerchantFile;
import it.gov.pagopa.merchant.repository.MerchantFileRepository;
import it.gov.pagopa.merchant.repository.MerchantRepository;
import it.gov.pagopa.merchant.utils.AuditUtilities;
import it.gov.pagopa.merchant.utils.Utilities;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Slf4j
public class UploadingMerchantServiceImpl implements UploadingMerchantService {

    public static final String COMMA = ",";
    public static final String MERCHANT = "merchant";
    public static final int BUSINESS_NAME_INDEX = 0;
    public static final int LEGAL_OFFICE_ADDRESS_INDEX = 1;
    public static final int LEGAL_OFFICE_MUNICIPALITY_INDEX = 2;
    public static final int LEGAL_OFFICE_PROVINCE_INDEX = 3;
    public static final int LEGAL_OFFICE_ZIP_INDEX = 4;
    public static final int EMAIL_INDEX = 5;
    public static final int FISCAL_CODE_INDEX = 6;
    public static final int VAT_INDEX = 7;
    public static final int IBAN_INDEX = 16;
    public static final String FISCAL_CODE_STRUCTURE_REGEX = "^([A-Za-z]{6}[0-9lmnpqrstuvLMNPQRSTUV]{2}[abcdehlmprstABCDEHLMPRST]{1}[0-9lmnpqrstuvLMNPQRSTUV]{2}[A-Za-z]{1}[0-9lmnpqrstuvLMNPQRSTUV]{3}[A-Za-z]{1})$";
    public static final String VAT_STRUCTURE_REGEX = "(^[0-9]{11})$";
    public static final String IBAN_STRUCTURE_REGEX = "[a-zA-Z]{2}[0-9]{2}[a-zA-Z0-9]{4}[0-9]{7}([a-zA-Z0-9]?){0,16}$";
    public static final String EMAIL_STRUCTURE_REGEX = "^[(a-zA-Z0-9-\\_\\.!\\D)]+@[(a-zA-Z)]+\\.[(a-zA-Z)]{2,3}$";

    @Autowired
    private MerchantFileRepository merchantFileRepository;
    @Autowired
    private MerchantRepository merchantRepository;
    @Autowired
    private static InitiativeRestConnector initiativeRestConnector;
    @Autowired
    private FileStorageConnector fileStorageConnector;
    @Autowired
    private Utilities utilities;
    @Autowired
    private AuditUtilities auditUtilities;

    @Value("${file.storage.path}")
    private String rootPath;


    @Override
    public MerchantUpdateDTO uploadMerchantFile(MultipartFile file, String organizationId, String initiativeId) {
        MerchantUpdateDTO merchantUpdateDTO = fileValidation(file, organizationId, initiativeId);

        if (MerchantConstants.Status.VALIDATED.equals(merchantUpdateDTO.getStatus())) {
            storeMerchantFile(organizationId, initiativeId, file);
        }
        return merchantUpdateDTO;
    }


    private void saveMerchantFile(String fileName, String organizationId, String initiativeId, String status) {
        MerchantFile merchantFile =
                MerchantFile.builder()
                        .fileName(fileName)
                        .initiativeId(initiativeId)
                        .organizationId(organizationId)
                        .organizationUserId(getOrganizationUserId())
                        .status(status)
                        .uploadDate(LocalDateTime.now())
                        .enabled(true).build();

        merchantFileRepository.save(merchantFile);
    }

    private MerchantUpdateDTO fileValidation(MultipartFile file, String organizationId, String initiativeId) {
        if (file.isEmpty()) {
            log.info("[UPLOAD_FILE_MERCHANT] - Initiative: {}. File is empty", initiativeId);
            auditUtilities.logUploadMerchantKO(initiativeId, organizationId, file.getName(), "File is empty");
            return MerchantUpdateDTO.builder()
                    .status(MerchantConstants.Status.KO)
                    .errorKey(MerchantConstants.Status.KOkeyMessage.INVALID_FILE_EMPTY)
                    .elabTimeStamp(LocalDateTime.now()).build();
        }
        if (!(MerchantConstants.CONTENT_TYPE.equals(file.getContentType()))) {
            log.info("[UPLOAD_FILE_MERCHANT] - Initiative: {}. ContentType not accepted: {}", initiativeId, file.getContentType());
            auditUtilities.logUploadMerchantKO(initiativeId, organizationId, file.getName(), "ContentType not accepted");
            return MerchantUpdateDTO.builder()
                    .status(MerchantConstants.Status.KO)
                    .errorKey(MerchantConstants.Status.KOkeyMessage.INVALID_FILE_FORMAT)
                    .elabTimeStamp(LocalDateTime.now()).build();
        }
        List<MerchantFile> merchantFileList = merchantFileRepository.findByFileNameAndInitiativeId(file.getOriginalFilename(), initiativeId);
        if (!merchantFileList.isEmpty()) {
            log.info("[UPLOAD_FILE_MERCHANT] - Initiative: {}. File name already used: {}", initiativeId, file.getOriginalFilename());
            auditUtilities.logUploadMerchantKO(initiativeId, organizationId, file.getName(), "File name already used");
            return MerchantUpdateDTO.builder()
                    .status(MerchantConstants.Status.KO)
                    .errorKey(MerchantConstants.Status.KOkeyMessage.INVALID_FILE_NAME)
                    .elabTimeStamp(LocalDateTime.now()).build();
        }

        String line;
        int lineNumber = 0;

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()));

            while ((line = br.readLine()) != null) {
                lineNumber++;
                String[] splitStr = line.split(COMMA);

                String[] controlString = Arrays.copyOfRange(splitStr, 0, FISCAL_CODE_INDEX);
                Arrays.fill(controlString, splitStr[IBAN_INDEX]);

                if (!Arrays.stream(controlString).allMatch(Objects::nonNull)) {
                    log.info("[UPLOAD_FILE_MERCHANT] - Initiative: {}. Missing required fields", initiativeId);
                    auditUtilities.logUploadMerchantKO(initiativeId, organizationId, file.getName(), "Missing required fields");
                    return MerchantUpdateDTO.builder().status(MerchantConstants.Status.KO).errorRow(lineNumber).errorKey(MerchantConstants.Status.KOkeyMessage.MISSING_REQUIRED_FIELDS).elabTimeStamp(LocalDateTime.now()).build();
                }

                if (!splitStr[FISCAL_CODE_INDEX].matches(FISCAL_CODE_STRUCTURE_REGEX) && !splitStr[FISCAL_CODE_INDEX].matches(VAT_STRUCTURE_REGEX)) {
                    log.info("[UPLOAD_FILE_MERCHANT] - Initiative: {}. Invalid fiscal code: {}", initiativeId, splitStr[FISCAL_CODE_INDEX]);
                    auditUtilities.logUploadMerchantKO(initiativeId, organizationId, file.getName(), "Invalid fiscal code");
                    return MerchantUpdateDTO.builder().status(MerchantConstants.Status.KO).errorRow(lineNumber).errorKey(MerchantConstants.Status.KOkeyMessage.INVALID_FILE_CF_WRONG).elabTimeStamp(LocalDateTime.now()).build();
                }

                if (!splitStr[IBAN_INDEX].matches(IBAN_STRUCTURE_REGEX)) {
                    log.info("[UPLOAD_FILE_MERCHANT] - Initiative: {}. Invalid iban: {}", initiativeId, splitStr[IBAN_INDEX]);
                    auditUtilities.logUploadMerchantKO(initiativeId, organizationId, file.getName(), "Invalid iban");
                    return MerchantUpdateDTO.builder().status(MerchantConstants.Status.KO).errorRow(lineNumber).errorKey(MerchantConstants.Status.KOkeyMessage.INVALID_FILE_IBAN_WRONG).elabTimeStamp(LocalDateTime.now()).build();
                }

                if (!splitStr[EMAIL_INDEX].matches(EMAIL_STRUCTURE_REGEX)) {
                    log.info("[UPLOAD_FILE_MERCHANT] - Initiative: {}. Invalid certified email: {}", initiativeId, splitStr[EMAIL_INDEX]);
                    auditUtilities.logUploadMerchantKO(initiativeId, organizationId, file.getName(), "Invalid certified email");
                    return MerchantUpdateDTO.builder().status(MerchantConstants.Status.KO).errorRow(lineNumber).errorKey(MerchantConstants.Status.KOkeyMessage.INVALID_FILE_EMAIL_WRONG).elabTimeStamp(LocalDateTime.now()).build();
                }
            }
        } catch (Exception e) {
            log.error("[UPLOAD_FILE_MERCHANT] - Generic Error: {}", e.getMessage());
            auditUtilities.logUploadMerchantKO(initiativeId, organizationId, file.getName(), e.getMessage());
            throw new ClientExceptionWithBody(HttpStatus.INTERNAL_SERVER_ERROR,
                    MerchantConstants.INTERNAL_SERVER_ERROR,
                    String.format(MerchantConstants.CSV_READING_ERROR, initiativeId, file.getOriginalFilename(), e.getMessage()));
        }
        auditUtilities.logUploadMerchantOK(initiativeId, organizationId, file.getName());
        return MerchantUpdateDTO.builder()
                .status(MerchantConstants.Status.VALIDATED)
                .elabTimeStamp(LocalDateTime.now()).build();
    }


    public void storeMerchantFile(String organizationId, String initiativeId, MultipartFile file) {
        long startTime = System.currentTimeMillis();
        try {
            InputStream inputStreamFile = file.getInputStream();
            fileStorageConnector.uploadMerchantFile(inputStreamFile, String.format(MerchantConstants.MERCHANT_FILE_PATH_TEMPLATE, organizationId, initiativeId, file.getOriginalFilename()), file.getContentType());
            saveMerchantFile(file.getOriginalFilename(), organizationId, initiativeId, MerchantConstants.Status.ON_EVALUATION);
            utilities.performanceLog(startTime, "STORE_MERCHANT_FILE");
        } catch (Exception e) {
            saveMerchantFile(file.getOriginalFilename(), organizationId, initiativeId, MerchantConstants.Status.KO);
            utilities.performanceLog(startTime, "STORE_MERCHANT_FILE");
            throw  new ClientExceptionWithBody(HttpStatus.INTERNAL_SERVER_ERROR,
                    MerchantConstants.INTERNAL_SERVER_ERROR,
                    String.format(MerchantConstants.STORAGE_ERROR, initiativeId, file.getOriginalFilename()));
        }
    }

    @Override
    public void ingestionMerchantFile(StorageEventDTO storageEventDto) {
        String[] urlPathSplits = storageEventDto.getData().getUrl().split("/");
        if (MERCHANT.equals(urlPathSplits[2])) {
            String fileName = urlPathSplits[5];
            String organizationId = urlPathSplits[3];
            String initiativeId = urlPathSplits[4];
            ByteArrayOutputStream downloadedMerchantFile = downloadMerchantFile(fileName, organizationId, initiativeId);
            saveMerchants(downloadedMerchantFile, fileName, organizationId, initiativeId);
            merchantFileRepository.setMerchantFileStatus(initiativeId, fileName, MerchantConstants.Status.VALIDATED);
        }
    }

    public ByteArrayOutputStream downloadMerchantFile(String fileName, String organizationId, String initiativeId) {
        //long startTime = System.currentTimeMillis();
        try {
            String fileNamePath = String.format(MerchantConstants.MERCHANT_FILE_PATH_TEMPLATE, organizationId, initiativeId, fileName);
            return fileStorageConnector.downloadMerchantFile(fileNamePath);
            //performanceLog(startTime, "DOWNLOAD_MERCHANT_FILE");
        } catch (Exception e) {
            //performanceLog(startTime, "DOWNLOAD_MERCHANT_FILE");
            merchantFileRepository.setMerchantFileStatus(initiativeId, fileName, MerchantConstants.Status.KO);
            throw new ClientExceptionWithBody(HttpStatus.INTERNAL_SERVER_ERROR,
                    MerchantConstants.INTERNAL_SERVER_ERROR,
                    String.format(MerchantConstants.DOWNLOAD_ERROR, initiativeId, fileName));
        }
    }

    public void saveMerchants(ByteArrayOutputStream byteFile, String fileName, String organizationId, String initiativeId) {

        byte[] bytes = byteFile.toByteArray();
        String line;

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes)));

            while ((line = br.readLine()) != null) {
                String[] splitStr = line.split(COMMA);

                Merchant merchant = merchantRepository.findByFiscalCodeAndAcquirerId(splitStr[FISCAL_CODE_INDEX], "pagopa")
                        .orElse(createNewMerchant(splitStr, initiativeId, organizationId));

                String ibanNew = splitStr[IBAN_INDEX];
                String ibanOld = merchant.getIban();
                boolean existsMerchantInitiative = merchant.getInitiativeList().stream().anyMatch(i -> i.getInitiativeId().equals(initiativeId));
                if (!existsMerchantInitiative){
                    merchant.getInitiativeList().add(merchantInitiativeCreation(initiativeId, organizationId));
                }
                if(!ibanNew.equals(ibanOld)){
                    merchant.setBusinessName(splitStr[BUSINESS_NAME_INDEX]);
                    merchant.setLegalOfficeAddress(splitStr[LEGAL_OFFICE_ADDRESS_INDEX]);
                    merchant.setLegalOfficeMunicipality(splitStr[LEGAL_OFFICE_MUNICIPALITY_INDEX]);
                    merchant.setLegalOfficeProvince(splitStr[LEGAL_OFFICE_PROVINCE_INDEX]);
                    merchant.setLegalOfficeZipCode(splitStr[LEGAL_OFFICE_ZIP_INDEX]);
                    merchant.setCertifiedEmail(splitStr[EMAIL_INDEX]);
                    merchant.setVatNumber(splitStr[VAT_INDEX]);
                    merchant.setIban(splitStr[IBAN_INDEX]);
                    merchant.setEnabled(true);
                }
                merchantRepository.save(merchant);
            }
        } catch (Exception e) {
            merchantFileRepository.setMerchantFileStatus(initiativeId, fileName, MerchantConstants.Status.KO);
            log.error("[SAVING_MERCHANT] - Generic Error: {}", e.getMessage());
            auditUtilities.logUploadMerchantKO(initiativeId, organizationId, fileName, e.getMessage());
            throw new ClientExceptionWithBody(HttpStatus.INTERNAL_SERVER_ERROR,
                    MerchantConstants.INTERNAL_SERVER_ERROR,
                    String.format(MerchantConstants.MERCHANT_SAVING_ERROR, initiativeId, fileName));
        }
    }

    private Merchant createNewMerchant(String[] splitStr, String initiativeId, String organizationId) {
        return Merchant.builder()
                .merchantId(Utilities.calculateSHA256Hash(splitStr[FISCAL_CODE_INDEX], "pagopa"))
                .acquirerId("pagopa")
                .businessName(splitStr[BUSINESS_NAME_INDEX])
                .legalOfficeAddress(splitStr[LEGAL_OFFICE_ADDRESS_INDEX])
                .legalOfficeMunicipality(splitStr[LEGAL_OFFICE_MUNICIPALITY_INDEX])
                .legalOfficeProvince(splitStr[LEGAL_OFFICE_PROVINCE_INDEX])
                .legalOfficeZipCode(splitStr[LEGAL_OFFICE_ZIP_INDEX])
                .certifiedEmail(splitStr[EMAIL_INDEX])
                .fiscalCode(splitStr[FISCAL_CODE_INDEX])
                .vatNumber(splitStr[VAT_INDEX])
                .iban(splitStr[IBAN_INDEX])
                .initiativeList(List.of(merchantInitiativeCreation(initiativeId, organizationId)))
                .enabled(true)
                .build();
    }

    private static Initiative merchantInitiativeCreation(String initiativeId, String organizationId) {
        String initiativeName = null;
        try{
            initiativeName = initiativeRestConnector.getInitiativeBeneficiaryView(initiativeId).getInitiativeName();
        } catch (Exception e){
            log.error("[INITIATIVE REST CONNECTOR] - General exception: {}", e.getMessage());
            throw new ClientExceptionNoBody(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong", e);
        }
        return Initiative.builder()
                .initiativeId(initiativeId)
                .initiativeName(initiativeName)
                .organizationId(organizationId)
                .merchantStatus("UPLOADED")
                .creationDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .enabled(true).build();
    }

    private String getOrganizationUserId(){
        String userId = null;
        if(RequestContextHolder.getRequestAttributes()!=null) {
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            if(requestAttributes != null) {
                userId = (String) requestAttributes.getAttribute("organizationUserId",
                        RequestAttributes.SCOPE_REQUEST);
            }
        }
        return userId;
    }

}
