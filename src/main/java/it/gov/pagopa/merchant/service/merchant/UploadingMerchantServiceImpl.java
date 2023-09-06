package it.gov.pagopa.merchant.service.merchant;

import it.gov.pagopa.merchant.connector.file_storage.FileStorageConnector;
import it.gov.pagopa.merchant.connector.initiative.InitiativeRestConnector;
import it.gov.pagopa.merchant.constants.MerchantConstants;
import it.gov.pagopa.merchant.dto.MerchantUpdateDTO;
import it.gov.pagopa.merchant.dto.QueueCommandOperationDTO;
import it.gov.pagopa.merchant.dto.StorageEventDTO;
import it.gov.pagopa.merchant.dto.initiative.InitiativeBeneficiaryViewDTO;
import it.gov.pagopa.merchant.event.producer.CommandsProducer;
import it.gov.pagopa.common.web.exception.ClientExceptionNoBody;
import it.gov.pagopa.common.web.exception.ClientExceptionWithBody;
import it.gov.pagopa.merchant.model.Initiative;
import it.gov.pagopa.merchant.model.Merchant;
import it.gov.pagopa.merchant.model.MerchantFile;
import it.gov.pagopa.merchant.repository.MerchantFileRepository;
import it.gov.pagopa.merchant.repository.MerchantRepository;
import it.gov.pagopa.merchant.utils.AuditUtilities;
import it.gov.pagopa.merchant.utils.Utilities;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class UploadingMerchantServiceImpl implements UploadingMerchantService {

    public static final String COMMA = ";";
    public static final String MERCHANT = "merchant";
    public static final int ACQUIRER_INDEX = 0;
    public static final int BUSINESS_NAME_INDEX = 1;
    public static final int LEGAL_OFFICE_ADDRESS_INDEX = 2;
    public static final int LEGAL_OFFICE_MUNICIPALITY_INDEX = 3;
    public static final int LEGAL_OFFICE_PROVINCE_INDEX = 4;
    public static final int LEGAL_OFFICE_ZIP_INDEX = 5;
    public static final int EMAIL_INDEX = 6;
    public static final int FISCAL_CODE_INDEX = 7;
    public static final int VAT_INDEX = 8;
    public static final int IBAN_INDEX = 17;
    public static final String FISCAL_CODE_STRUCTURE_REGEX = "(^([A-Za-z]{6}[0-9lmnpqrstuvLMNPQRSTUV]{2}[abcdehlmprstABCDEHLMPRST][0-9lmnpqrstuvLMNPQRSTUV]{2}[A-Za-z][0-9lmnpqrstuvLMNPQRSTUV]{3}[A-Za-z])$)|(^(\\d{11})$)";
    public static final String IBAN_STRUCTURE_REGEX = "^(it|IT)\\d{2}[A-Za-z]\\d{10}[0-9A-Za-z]{12}$";
    public static final String EMAIL_STRUCTURE_REGEX = "^[a-zA-Z0-9-_.!]+@[(a-zA-Z)]+\\.[(a-zA-Z)]{2,3}$";
    private final MerchantFileRepository merchantFileRepository;

    private final MerchantRepository merchantRepository;

    private final InitiativeRestConnector initiativeRestConnector;

    private final FileStorageConnector fileStorageConnector;

    private final AuditUtilities auditUtilities;
    private final CommandsProducer commandsProducer;

    public UploadingMerchantServiceImpl(MerchantFileRepository merchantFileRepository,
                                        MerchantRepository merchantRepository,
                                        InitiativeRestConnector initiativeRestConnector,
                                        FileStorageConnector fileStorageConnector,
                                        AuditUtilities auditUtilities, CommandsProducer commandsProducer) {
        this.merchantFileRepository = merchantFileRepository;
        this.merchantRepository = merchantRepository;
        this.initiativeRestConnector = initiativeRestConnector;
        this.fileStorageConnector = fileStorageConnector;
        this.auditUtilities = auditUtilities;
        this.commandsProducer = commandsProducer;
    }

    @Override
    public MerchantUpdateDTO uploadMerchantFile(MultipartFile file, String entityId, String initiativeId, String organizationUserId, String acquirerId) {
        log.info("[UPLOAD_FILE_MERCHANT] - Starting uploading file {} for initiative {}", file.getOriginalFilename(), initiativeId);
        MerchantUpdateDTO merchantUpdateDTO = fileValidation(file, entityId, initiativeId, acquirerId);

        if (MerchantConstants.Status.VALIDATED.equals(merchantUpdateDTO.getStatus())) {
            storeMerchantFile(entityId, initiativeId, file, organizationUserId);
            auditUtilities.logUploadMerchantOK(initiativeId, entityId, file.getOriginalFilename());
        }
        return merchantUpdateDTO;
    }

    private MerchantUpdateDTO fileValidation(MultipartFile file, String entityId, String initiativeId, String acquirerId) {
        if (file.isEmpty()) {
            log.info("[UPLOAD_FILE_MERCHANT] - Initiative: {}. File is empty", initiativeId);
            auditUtilities.logUploadMerchantKO(initiativeId, entityId, file.getName(), "File is empty");
            return toMerchantUpdateKO(MerchantConstants.Status.KOkeyMessage.INVALID_FILE_EMPTY, null);
        }
        if (!(MerchantConstants.CONTENT_TYPE.equals(file.getContentType()))) {
            log.info("[UPLOAD_FILE_MERCHANT] - Initiative: {}. ContentType not accepted: {}", initiativeId, file.getContentType());
            auditUtilities.logUploadMerchantKO(initiativeId, entityId, file.getName(), "ContentType not accepted");
            return toMerchantUpdateKO(MerchantConstants.Status.KOkeyMessage.INVALID_FILE_FORMAT, null);
        }
        List<MerchantFile> merchantFileList = merchantFileRepository.findByFileNameAndInitiativeId(file.getOriginalFilename(), initiativeId);
        if (!merchantFileList.isEmpty()) {
            log.info("[UPLOAD_FILE_MERCHANT] - Initiative: {}. File name already used: {}", initiativeId, file.getOriginalFilename());
            auditUtilities.logUploadMerchantKO(initiativeId, entityId, file.getName(), "File name already used");
            return toMerchantUpdateKO(MerchantConstants.Status.KOkeyMessage.INVALID_FILE_NAME, null);
        }

        int lineNumber = 1;

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()));
            List<String> lines = br.lines().skip(1).toList();
            for(String line : lines) {
                lineNumber++;

                String[] splitStr = line.split(COMMA, -1);

                List<String> controlString = new ArrayList<>(List.of(Arrays.copyOfRange(splitStr, 0, FISCAL_CODE_INDEX)));
                controlString.add(splitStr[IBAN_INDEX]);

                if (controlString.stream().anyMatch(StringUtils::isBlank)) {
                    log.info("[UPLOAD_FILE_MERCHANT] - Initiative: {}. Missing required fields", initiativeId);
                    auditUtilities.logUploadMerchantKO(initiativeId, entityId, file.getName(), "Missing required fields");
                    return toMerchantUpdateKO(MerchantConstants.Status.KOkeyMessage.MISSING_REQUIRED_FIELDS, lineNumber);
                }

                if(!splitStr[ACQUIRER_INDEX].equals(acquirerId)) {
                    log.info("[UPLOAD_FILE_MERCHANT] - Initiative: {}. Invalid acquirer Id: {}", initiativeId, splitStr[ACQUIRER_INDEX]);
                    auditUtilities.logUploadMerchantKO(initiativeId, entityId, file.getName(), "Invalid acquirer Id");
                    return toMerchantUpdateKO(MerchantConstants.Status.KOkeyMessage.INVALID_FILE_ACQUIRER_WRONG, lineNumber);
                }

                if (!splitStr[FISCAL_CODE_INDEX].matches(FISCAL_CODE_STRUCTURE_REGEX)) {
                    log.info("[UPLOAD_FILE_MERCHANT] - Initiative: {}. Invalid fiscal code: {}", initiativeId, splitStr[FISCAL_CODE_INDEX]);
                    auditUtilities.logUploadMerchantKO(initiativeId, entityId, file.getName(), "Invalid fiscal code");
                    return toMerchantUpdateKO(MerchantConstants.Status.KOkeyMessage.INVALID_FILE_CF_WRONG, lineNumber);
                }

                if (!splitStr[IBAN_INDEX].matches(IBAN_STRUCTURE_REGEX)) {
                    log.info("[UPLOAD_FILE_MERCHANT] - Initiative: {}. Invalid iban: {}", initiativeId, splitStr[IBAN_INDEX]);
                    auditUtilities.logUploadMerchantKO(initiativeId, entityId, file.getName(), "Invalid iban");
                    return toMerchantUpdateKO(MerchantConstants.Status.KOkeyMessage.INVALID_FILE_IBAN_WRONG, lineNumber);
                }

                if (!splitStr[EMAIL_INDEX].matches(EMAIL_STRUCTURE_REGEX)) {
                    log.info("[UPLOAD_FILE_MERCHANT] - Initiative: {}. Invalid certified email: {}", initiativeId, splitStr[EMAIL_INDEX]);
                    auditUtilities.logUploadMerchantKO(initiativeId, entityId, file.getName(), "Invalid certified email");
                    return toMerchantUpdateKO(MerchantConstants.Status.KOkeyMessage.INVALID_FILE_EMAIL_WRONG, lineNumber);
                }
            }
            br.close();
        } catch (Exception e) {
            log.error("[UPLOAD_FILE_MERCHANT] - Generic Error: {}", e.getMessage());
            auditUtilities.logUploadMerchantKO(initiativeId, entityId, file.getName(), e.getMessage());
            throw new ClientExceptionWithBody(HttpStatus.INTERNAL_SERVER_ERROR,
                    MerchantConstants.INTERNAL_SERVER_ERROR,
                    String.format(MerchantConstants.CSV_READING_ERROR, initiativeId, file.getOriginalFilename(), e.getMessage()));
        }
        log.info("[UPLOAD_FILE_MERCHANT] - Initiative: {}. Merchants file {} validated", initiativeId, file.getOriginalFilename());
        auditUtilities.logValidationMerchantOK(initiativeId, entityId, file.getName());
        return MerchantUpdateDTO.builder()
                .status(MerchantConstants.Status.VALIDATED)
                .elabTimeStamp(LocalDateTime.now()).build();
    }


    public void storeMerchantFile(String entityId, String initiativeId, MultipartFile file, String organizationUserId) {
        long startTime = System.currentTimeMillis();
        try {
            log.info("[UPLOAD_FILE_MERCHANT] - Initiative: {}. Merchants file {} sent to storage", initiativeId, file.getOriginalFilename());
            InputStream inputStreamFile = file.getInputStream();
            fileStorageConnector.uploadMerchantFile(inputStreamFile, String.format(MerchantConstants.MERCHANT_FILE_PATH_TEMPLATE, entityId, initiativeId, file.getOriginalFilename()), file.getContentType());
            saveMerchantFile(file.getOriginalFilename(), entityId, initiativeId, organizationUserId, MerchantConstants.Status.ON_EVALUATION);
            Utilities.performanceLog(startTime, "STORE_MERCHANT_FILE");
        } catch (Exception e) {
            log.info("[UPLOAD_FILE_MERCHANT] - Initiative: {}. Merchants file {} storage failed", initiativeId, file.getOriginalFilename());
            auditUtilities.logUploadMerchantKO(initiativeId, entityId, file.getOriginalFilename(), "Error during file storage");
            saveMerchantFile(file.getOriginalFilename(), entityId, initiativeId, organizationUserId, MerchantConstants.Status.STORAGE_KO);
            Utilities.performanceLog(startTime, "STORE_MERCHANT_FILE");
            throw  new ClientExceptionWithBody(HttpStatus.INTERNAL_SERVER_ERROR,
                    MerchantConstants.INTERNAL_SERVER_ERROR,
                    String.format(MerchantConstants.STORAGE_ERROR, initiativeId, file.getOriginalFilename()));
        }
    }

    private void saveMerchantFile(String fileName, String organizationId, String initiativeId, String organizationUserId, String status) {
        MerchantFile merchantFile =
                MerchantFile.builder()
                        .fileName(fileName)
                        .initiativeId(initiativeId)
                        .entityId(organizationId)
                        .organizationUserId(organizationUserId)
                        .status(status)
                        .uploadDate(LocalDateTime.now())
                        .enabled(true).build();

        merchantFileRepository.save(merchantFile);
    }
    @Override
    public void ingestionMerchantFile(List<StorageEventDTO> storageEventDTOList) {
        log.info("[SAVE_MERCHANTS] - Saving merchants started");
        StorageEventDTO storageEventDTO = storageEventDTOList.stream().findFirst().orElse(null);
        if (storageEventDTO != null && StringUtils.isNotBlank(storageEventDTO.getSubject())) {
            String[] subjectPathSplit = storageEventDTO.getSubject().split("/");
            if (MERCHANT.equals(subjectPathSplit[4]) && subjectPathSplit.length == 9) {
                String fileName = subjectPathSplit[8];
                String entityId = subjectPathSplit[6];
                String initiativeId = subjectPathSplit[7];
                ByteArrayOutputStream downloadedMerchantFile = downloadMerchantFile(fileName, entityId, initiativeId);
                saveMerchants(downloadedMerchantFile, fileName, entityId, initiativeId);
                merchantFileRepository.setMerchantFileStatus(initiativeId, fileName, MerchantConstants.Status.PROCESSED);
                log.info("[SAVE_MERCHANTS] - Initiative: {} - file {}. Saving merchants completed", initiativeId, fileName);
                auditUtilities.logSavingMerchantsOK(initiativeId, entityId, fileName);
            }
        }
    }

    public ByteArrayOutputStream downloadMerchantFile(String fileName, String organizationId, String initiativeId) {
        long startTime = System.currentTimeMillis();
        try {
            log.info("[SAVE_MERCHANTS] - Initiative: {}. Downloading merchants file {}", initiativeId, fileName);
            String fileNamePath = String.format(MerchantConstants.MERCHANT_FILE_PATH_TEMPLATE, organizationId, initiativeId, fileName);
            ByteArrayOutputStream merchantFile = fileStorageConnector.downloadMerchantFile(fileNamePath);
            Utilities.performanceLog(startTime, "DOWNLOAD_MERCHANT_FILE");
            return merchantFile;
        } catch (Exception e) {
            log.info("[SAVE_MERCHANTS] - Initiative: {}. Merchants file {} download failed", initiativeId, fileName);
            auditUtilities.logUploadMerchantKO(initiativeId, organizationId, fileName, e.getMessage());
            merchantFileRepository.setMerchantFileStatus(initiativeId, fileName, MerchantConstants.Status.DOWNLOAD_KO);
            Utilities.performanceLog(startTime, "DOWNLOAD_MERCHANT_FILE");
            throw new ClientExceptionWithBody(HttpStatus.INTERNAL_SERVER_ERROR,
                    MerchantConstants.INTERNAL_SERVER_ERROR,
                    String.format(MerchantConstants.DOWNLOAD_ERROR, initiativeId, fileName));
        }
    }

    public void saveMerchants(ByteArrayOutputStream byteFile, String fileName, String entityId, String initiativeId) {
        long startTime = System.currentTimeMillis();

        InitiativeBeneficiaryViewDTO initiativeDTO = getInitiativeInfo(initiativeId);

        try {
            log.info("[SAVE_MERCHANTS] - Initiative: {} - file {}. Saving merchants", initiativeId, fileName);
            byte[] bytes = byteFile.toByteArray();
            BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes)));

            br.lines().skip(1).forEach(line -> {
                String[] splitStr = line.split(COMMA);
                Merchant merchant = merchantRepository.findByFiscalCodeAndAcquirerId(splitStr[FISCAL_CODE_INDEX], splitStr[ACQUIRER_INDEX])
                        .orElse(createNewMerchant(splitStr));

                String ibanNew = splitStr[IBAN_INDEX];
                String ibanOld = merchant.getIban();
                boolean existsMerchantInitiative = merchant.getInitiativeList().stream().anyMatch(i -> i.getInitiativeId().equals(initiativeId));
                if (!existsMerchantInitiative) {
                    merchant.getInitiativeList().add(createMerchantInitiative(initiativeDTO));
                }
                if (!ibanNew.equals(ibanOld)) {
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
                initializeMerchantStatistics(initiativeId, merchant.getMerchantId());
            });
            Utilities.performanceLog(startTime, "SAVE_MERCHANTS");
        } catch (Exception e) {
            log.info("[SAVE_MERCHANTS] - Initiative: {} - file: {}. Merchants saving failed: {}", initiativeId, fileName, e);
            merchantFileRepository.setMerchantFileStatus(initiativeId, fileName, MerchantConstants.Status.MERCHANT_SAVING_KO);
            auditUtilities.logUploadMerchantKO(initiativeId, entityId, fileName, e.getMessage());
            Utilities.performanceLog(startTime, "SAVE_MERCHANTS");
            throw new ClientExceptionWithBody(HttpStatus.INTERNAL_SERVER_ERROR,
                    MerchantConstants.INTERNAL_SERVER_ERROR,
                    String.format(MerchantConstants.MERCHANT_SAVING_ERROR, initiativeId, fileName));
        }
    }

    public InitiativeBeneficiaryViewDTO getInitiativeInfo(String initiativeId) {
        InitiativeBeneficiaryViewDTO initiativeDTO;
        try {
            initiativeDTO = initiativeRestConnector.getInitiativeBeneficiaryView(initiativeId);
        } catch (Exception e) {
            log.error("[INITIATIVE REST CONNECTOR] - General exception: {}", e.getMessage());
            throw new ClientExceptionNoBody(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong", e);
        }
        return initiativeDTO;
    }

    private Merchant createNewMerchant(String[] splitStr) {
        return Merchant.builder()
                .merchantId(Utilities.toUUID(splitStr[FISCAL_CODE_INDEX].concat("_").concat(splitStr[ACQUIRER_INDEX])))
                .acquirerId(splitStr[ACQUIRER_INDEX])
                .businessName(splitStr[BUSINESS_NAME_INDEX])
                .legalOfficeAddress(splitStr[LEGAL_OFFICE_ADDRESS_INDEX])
                .legalOfficeMunicipality(splitStr[LEGAL_OFFICE_MUNICIPALITY_INDEX])
                .legalOfficeProvince(splitStr[LEGAL_OFFICE_PROVINCE_INDEX])
                .legalOfficeZipCode(splitStr[LEGAL_OFFICE_ZIP_INDEX])
                .certifiedEmail(splitStr[EMAIL_INDEX])
                .fiscalCode(splitStr[FISCAL_CODE_INDEX])
                .vatNumber(splitStr[VAT_INDEX])
                .iban(splitStr[IBAN_INDEX])
                .initiativeList(new ArrayList<>())
                .enabled(true)
                .build();
    }

    private Initiative createMerchantInitiative(InitiativeBeneficiaryViewDTO initiativeDTO) {
        return Initiative.builder()
                .initiativeId(initiativeDTO.getInitiativeId())
                .initiativeName(initiativeDTO.getInitiativeName())
                .organizationId(initiativeDTO.getOrganizationId())
                .organizationName(initiativeDTO.getOrganizationName())
                .serviceId(initiativeDTO.getAdditionalInfo().getServiceId())
                .startDate(initiativeDTO.getGeneral().getStartDate())
                .endDate(initiativeDTO.getGeneral().getEndDate())
                .status(initiativeDTO.getStatus())
                .merchantStatus("UPLOADED")
                .creationDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .enabled(true).build();
    }

    private MerchantUpdateDTO toMerchantUpdateKO(String errorKey, Integer errorRow){
        return MerchantUpdateDTO.builder()
                .status(MerchantConstants.Status.KO)
                .errorKey(errorKey)
                .errorRow(errorRow)
                .elabTimeStamp(LocalDateTime.now()).build();
    }

    private void initializeMerchantStatistics(String initiativeId, String merchantId) {
        QueueCommandOperationDTO createMerchantStatistics = QueueCommandOperationDTO.builder()
                .entityId(initiativeId.concat("_").concat(merchantId))
                .operationType(MerchantConstants.OPERATION_TYPE_CREATE_MERCHANT_STATISTICS)
                .operationTime(LocalDateTime.now())
                .build();
        if(!commandsProducer.sendCommand(createMerchantStatistics)){
            log.error("[CREATE_MERCHANT_STATISTICS] - Initiative: {}. Something went wrong while sending the message on Commands Queue", initiativeId);
        }
    }
}
