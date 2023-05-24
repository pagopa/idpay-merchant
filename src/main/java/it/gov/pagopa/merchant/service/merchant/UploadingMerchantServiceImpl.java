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
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class UploadingMerchantServiceImpl implements UploadingMerchantService {

    public static final String COMMA = ";";
    public static final String MERCHANT = "merchant";
    public static final String PAGOPA = "PAGOPA";
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
    public static final String IBAN_STRUCTURE_REGEX = "^(it|IT)[0-9]{2}[A-Za-z][0-9]{10}[0-9A-Za-z]{12}$";
    public static final String EMAIL_STRUCTURE_REGEX = "^[(a-zA-Z0-9-\\_\\.!\\D)]+@[(a-zA-Z)]+\\.[(a-zA-Z)]{2,3}$";

    @Autowired
    private MerchantFileRepository merchantFileRepository;
    @Autowired
    private MerchantRepository merchantRepository;
    @Autowired
    InitiativeRestConnector initiativeRestConnector;
    @Autowired
    FileStorageConnector fileStorageConnector;
    @Autowired
    Utilities utilities;
    @Autowired
    AuditUtilities auditUtilities;


    @Override
    public MerchantUpdateDTO uploadMerchantFile(MultipartFile file, String organizationId, String initiativeId, String organizationUserId) {
        log.info("[UPLOAD_FILE_MERCHANT] - Starting uploading file {} for initiative {}", file.getOriginalFilename(), initiativeId);
        MerchantUpdateDTO merchantUpdateDTO = fileValidation(file, organizationId, initiativeId);

        if (MerchantConstants.Status.VALIDATED.equals(merchantUpdateDTO.getStatus())) {
            storeMerchantFile(organizationId, initiativeId, file, organizationUserId);
            auditUtilities.logUploadMerchantOK(initiativeId, organizationId, file.getOriginalFilename());
        }
        return merchantUpdateDTO;
    }


    private void saveMerchantFile(String fileName, String organizationId, String initiativeId, String organizationUserId, String status) {
        MerchantFile merchantFile =
                MerchantFile.builder()
                        .fileName(fileName)
                        .initiativeId(initiativeId)
                        .organizationId(organizationId)
                        .organizationUserId(organizationUserId)
                        .status(status)
                        .uploadDate(LocalDateTime.now())
                        .enabled(true).build();

        merchantFileRepository.save(merchantFile);
    }

    private MerchantUpdateDTO fileValidation(MultipartFile file, String organizationId, String initiativeId) {
        if (file.isEmpty()) {
            log.info("[UPLOAD_FILE_MERCHANT] - Initiative: {}. File is empty", initiativeId);
            auditUtilities.logUploadMerchantKO(initiativeId, organizationId, file.getName(), "File is empty");
            return toMerchantUpdateKO(MerchantConstants.Status.KOkeyMessage.INVALID_FILE_EMPTY, null);
        }
        if (!(MerchantConstants.CONTENT_TYPE.equals(file.getContentType()))) {
            log.info("[UPLOAD_FILE_MERCHANT] - Initiative: {}. ContentType not accepted: {}", initiativeId, file.getContentType());
            auditUtilities.logUploadMerchantKO(initiativeId, organizationId, file.getName(), "ContentType not accepted");
            return toMerchantUpdateKO(MerchantConstants.Status.KOkeyMessage.INVALID_FILE_FORMAT, null);
        }
        List<MerchantFile> merchantFileList = merchantFileRepository.findByFileNameAndInitiativeId(file.getOriginalFilename(), initiativeId);
        if (!merchantFileList.isEmpty()) {
            log.info("[UPLOAD_FILE_MERCHANT] - Initiative: {}. File name already used: {}", initiativeId, file.getOriginalFilename());
            auditUtilities.logUploadMerchantKO(initiativeId, organizationId, file.getName(), "File name already used");
            return toMerchantUpdateKO(MerchantConstants.Status.KOkeyMessage.INVALID_FILE_NAME, null);
        }

        String line;
        int lineNumber = 0;

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()));
            while ((line = br.readLine()) != null) {
                lineNumber++;
                if (lineNumber == 1){
                    continue; //skipping csv file header
                }
                String[] splitStr = line.split(COMMA, -1);

                List<String> controlString = new ArrayList<>(List.of(Arrays.copyOfRange(splitStr, 0, FISCAL_CODE_INDEX)));
                controlString.add(splitStr[IBAN_INDEX]);

                if (controlString.stream().anyMatch(StringUtils::isBlank)) {
                    log.info("[UPLOAD_FILE_MERCHANT] - Initiative: {}. Missing required fields", initiativeId);
                    auditUtilities.logUploadMerchantKO(initiativeId, organizationId, file.getName(), "Missing required fields");
                    return toMerchantUpdateKO(MerchantConstants.Status.KOkeyMessage.MISSING_REQUIRED_FIELDS, lineNumber);
                }

                if (!splitStr[FISCAL_CODE_INDEX].matches(FISCAL_CODE_STRUCTURE_REGEX) && !splitStr[FISCAL_CODE_INDEX].matches(VAT_STRUCTURE_REGEX)) {
                    log.info("[UPLOAD_FILE_MERCHANT] - Initiative: {}. Invalid fiscal code: {}", initiativeId, splitStr[FISCAL_CODE_INDEX]);
                    auditUtilities.logUploadMerchantKO(initiativeId, organizationId, file.getName(), "Invalid fiscal code");
                    return toMerchantUpdateKO(MerchantConstants.Status.KOkeyMessage.INVALID_FILE_CF_WRONG, lineNumber);
                }

                if (!splitStr[IBAN_INDEX].matches(IBAN_STRUCTURE_REGEX)) {
                    log.info("[UPLOAD_FILE_MERCHANT] - Initiative: {}. Invalid iban: {}", initiativeId, splitStr[IBAN_INDEX]);
                    auditUtilities.logUploadMerchantKO(initiativeId, organizationId, file.getName(), "Invalid iban");
                    return toMerchantUpdateKO(MerchantConstants.Status.KOkeyMessage.INVALID_FILE_IBAN_WRONG, lineNumber);
                }

                if (!splitStr[EMAIL_INDEX].matches(EMAIL_STRUCTURE_REGEX)) {
                    log.info("[UPLOAD_FILE_MERCHANT] - Initiative: {}. Invalid certified email: {}", initiativeId, splitStr[EMAIL_INDEX]);
                    auditUtilities.logUploadMerchantKO(initiativeId, organizationId, file.getName(), "Invalid certified email");
                    return toMerchantUpdateKO(MerchantConstants.Status.KOkeyMessage.INVALID_FILE_EMAIL_WRONG, lineNumber);
                }
            }
        } catch (Exception e) {
            log.error("[UPLOAD_FILE_MERCHANT] - Generic Error: {}", e.getMessage());
            auditUtilities.logUploadMerchantKO(initiativeId, organizationId, file.getName(), e.getMessage());
            throw new ClientExceptionWithBody(HttpStatus.INTERNAL_SERVER_ERROR,
                    MerchantConstants.INTERNAL_SERVER_ERROR,
                    String.format(MerchantConstants.CSV_READING_ERROR, initiativeId, file.getOriginalFilename(), e.getMessage()));
        }
        log.info("[UPLOAD_FILE_MERCHANT] - Initiative: {}. Merchants file {} validated", initiativeId, file.getOriginalFilename());
        auditUtilities.logValidationMerchantOK(initiativeId, organizationId, file.getName());
        return MerchantUpdateDTO.builder()
                .status(MerchantConstants.Status.VALIDATED)
                .elabTimeStamp(LocalDateTime.now()).build();
    }


    public void storeMerchantFile(String organizationId, String initiativeId, MultipartFile file, String organizationUserId) {
        long startTime = System.currentTimeMillis();
        try {
            log.info("[UPLOAD_FILE_MERCHANT] - Initiative: {}. Merchants file {} sent to storage", initiativeId, file.getOriginalFilename());
            InputStream inputStreamFile = file.getInputStream();
            fileStorageConnector.uploadMerchantFile(inputStreamFile, String.format(MerchantConstants.MERCHANT_FILE_PATH_TEMPLATE, organizationId, initiativeId, file.getOriginalFilename()), file.getContentType());
            saveMerchantFile(file.getOriginalFilename(), organizationId, initiativeId, organizationUserId, MerchantConstants.Status.ON_EVALUATION);
            utilities.performanceLog(startTime, "STORE_MERCHANT_FILE");
        } catch (Exception e) {
            log.info("[UPLOAD_FILE_MERCHANT] - Initiative: {}. Merchants file {} storage failed", initiativeId, file.getOriginalFilename());
            auditUtilities.logUploadMerchantKO(initiativeId, organizationId, file.getOriginalFilename(), "Error during file storage");
            saveMerchantFile(file.getOriginalFilename(), organizationId, initiativeId, organizationUserId, MerchantConstants.Status.STORAGE_KO);
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
            merchantFileRepository.setMerchantFileStatus(initiativeId, fileName, MerchantConstants.Status.PROCESSED);
            log.info("[SAVE_MERCHANTS] - Initiative: {} - file {}. Saving merchants completed", initiativeId, fileName);
            auditUtilities.logSavingMerchantsOK(initiativeId,organizationId, fileName);
        }
    }

    public ByteArrayOutputStream downloadMerchantFile(String fileName, String organizationId, String initiativeId) {
        long startTime = System.currentTimeMillis();
        try {
            log.info("[SAVE_MERCHANTS] - Initiative: {}. Downloading merchants file {}", initiativeId, fileName);
            String fileNamePath = String.format(MerchantConstants.MERCHANT_FILE_PATH_TEMPLATE, organizationId, initiativeId, fileName);
            ByteArrayOutputStream merchantFile = fileStorageConnector.downloadMerchantFile(fileNamePath);
            utilities.performanceLog(startTime, "DOWNLOAD_MERCHANT_FILE");
            return merchantFile;
        } catch (Exception e) {
            log.info("[SAVE_MERCHANTS] - Initiative: {}. Merchants file {} download failed", initiativeId, fileName);
            merchantFileRepository.setMerchantFileStatus(initiativeId, fileName, MerchantConstants.Status.DOWNLOAD_KO);
            utilities.performanceLog(startTime, "DOWNLOAD_MERCHANT_FILE");
            throw new ClientExceptionWithBody(HttpStatus.INTERNAL_SERVER_ERROR,
                    MerchantConstants.INTERNAL_SERVER_ERROR,
                    String.format(MerchantConstants.DOWNLOAD_ERROR, initiativeId, fileName));
        }
    }

    public void saveMerchants(ByteArrayOutputStream byteFile, String fileName, String organizationId, String initiativeId) {
        long startTime = System.currentTimeMillis();
        byte[] bytes = byteFile.toByteArray();
        String line;

        try {
            log.info("[SAVE_MERCHANTS] - Initiative: {} - file {}. Saving merchants", initiativeId, fileName);
            BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes)));

            List<Merchant> merchantList = new ArrayList<>();

            while ((line = br.readLine()) != null) {
                String[] splitStr = line.split(COMMA);

                Merchant merchant = merchantRepository.findByFiscalCodeAndAcquirerId(splitStr[FISCAL_CODE_INDEX], PAGOPA)
                        .orElse(createNewMerchant(splitStr));

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
                merchantList.add(merchant);
            }
            merchantRepository.saveAll(merchantList);
            utilities.performanceLog(startTime, "SAVE_MERCHANTS");
        } catch (Exception e) {
            log.info("[SAVE_MERCHANTS] - Initiative: {} - file: {}. Merchants saving failed: {}", initiativeId, fileName, e.getMessage());
            merchantFileRepository.setMerchantFileStatus(initiativeId, fileName, MerchantConstants.Status.MERCHANT_SAVING_KO);
            auditUtilities.logUploadMerchantKO(initiativeId, organizationId, fileName, e.getMessage());
            utilities.performanceLog(startTime, "SAVE_MERCHANTS");
            throw new ClientExceptionWithBody(HttpStatus.INTERNAL_SERVER_ERROR,
                    MerchantConstants.INTERNAL_SERVER_ERROR,
                    String.format(MerchantConstants.MERCHANT_SAVING_ERROR, initiativeId, fileName));
        }
    }

    private Merchant createNewMerchant(String[] splitStr) {
        return Merchant.builder()
                .merchantId(Utilities.calculateSHA256Hash(splitStr[FISCAL_CODE_INDEX], PAGOPA))
                .acquirerId(PAGOPA)
                .businessName(splitStr[BUSINESS_NAME_INDEX])
                .legalOfficeAddress(splitStr[LEGAL_OFFICE_ADDRESS_INDEX])
                .legalOfficeMunicipality(splitStr[LEGAL_OFFICE_MUNICIPALITY_INDEX])
                .legalOfficeProvince(splitStr[LEGAL_OFFICE_PROVINCE_INDEX])
                .legalOfficeZipCode(splitStr[LEGAL_OFFICE_ZIP_INDEX])
                .certifiedEmail(splitStr[EMAIL_INDEX])
                .fiscalCode(splitStr[FISCAL_CODE_INDEX])
                .vatNumber(splitStr[VAT_INDEX])
                .iban(splitStr[IBAN_INDEX])
                .initiativeList(Collections.emptyList())
                .enabled(true)
                .build();
    }

    private Initiative merchantInitiativeCreation(String initiativeId, String organizationId) {
        String initiativeName;
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

    private MerchantUpdateDTO toMerchantUpdateKO(String errorKey, Integer errorRow){
        return MerchantUpdateDTO.builder()
                .status(MerchantConstants.Status.KO)
                .errorKey(errorKey)
                .errorRow(errorRow)
                .elabTimeStamp(LocalDateTime.now()).build();
    }

}
