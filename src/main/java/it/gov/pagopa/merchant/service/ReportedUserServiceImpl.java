package it.gov.pagopa.merchant.service;

import it.gov.pagopa.merchant.connector.transaction.TransactionConnector;
import it.gov.pagopa.merchant.constants.ReportedUserExceptions;
import it.gov.pagopa.merchant.dto.ReportedUserDTO;
import it.gov.pagopa.merchant.dto.ReportedUserCreateResponseDTO;
import it.gov.pagopa.merchant.dto.transaction.RewardTransaction;
import it.gov.pagopa.merchant.mapper.ReportedUserMapper;
import it.gov.pagopa.merchant.model.ReportedUser;
import it.gov.pagopa.merchant.repository.ReportedUserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static it.gov.pagopa.merchant.constants.MerchantConstants.TransactionStatus.ALLOWED_TRANSACTION_STATUSES;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportedUserServiceImpl implements ReportedUserService {

    private static final Logger log = LoggerFactory.getLogger(ReportedUserServiceImpl.class);

    private final ReportedUserRepository repository;
    private final PDVService pdvService;
    private final ReportedUserMapper mapper;
    private final TransactionConnector transactionConnector;


    @Override
    public ReportedUserCreateResponseDTO createReportedUser(String userFiscalCode, String merchantId, String initiativeId) {

        log.info("[REPORTED_USER_CREATE] - Start create");

        String cleanedFiscalCode = sanitizeFiscalCode(userFiscalCode);

        String userId = pdvService.encryptCF(cleanedFiscalCode);

        if (userId == null || userId.isEmpty()) {
            return ReportedUserCreateResponseDTO.ko(ReportedUserExceptions.USERID_NOT_FOUND);
        }

        log.info("[REPORTED_USER_CREATE] - Get userId by fiscalCode");

        boolean alreadyReported = repository.existsByUserId(userId);
        if (alreadyReported) {
            log.info("[REPORTED_USER_CREATE] - User already reported");
            return ReportedUserCreateResponseDTO.ko(ReportedUserExceptions.ALREADY_REPORTED);
        }


        try {
            List<RewardTransaction> trxList = transactionConnector.findAll(null,
                    userId,
                    LocalDateTime.now().minusYears(1),
                    LocalDateTime.now(),
                    null,
                    PageRequest.of(0, 10));
            trxList = trxList.stream()
                    .filter(trx -> ALLOWED_TRANSACTION_STATUSES.contains(trx.getStatus()))
                    .filter(trx -> trx.getInitiatives() != null && trx.getInitiatives().contains(initiativeId))
                    .filter(trx -> merchantId.equals(trx.getMerchantId()))
                    .toList();

            if (trxList.isEmpty() || trxList.getFirst() == null ) {
                return ReportedUserCreateResponseDTO.ko(ReportedUserExceptions.ENTITY_NOT_FOUND);
            }

            RewardTransaction trx = trxList.getFirst();

            log.info("[REPORTED_USER_CREATE] - Get data by transaction = {}, ", trx);

            ReportedUser reportedUser = repository.save(ReportedUser.builder()
                    .createdAt(LocalDateTime.now())
                    .trxChargeDate(trx.getTrxChargeDate())
                    .transactionId(trx.getId())
                    .userId(userId)
                    .initiativeId(initiativeId)
                    .merchantId(merchantId)
                    .build());

            log.info("[REPORTED_USER_CREATE] - Created reported user with id={}", reportedUser.getReportedUserId());
            return ReportedUserCreateResponseDTO.ok();

        } catch (Exception e) {
            log.error("[REPORTED_USER_CREATE] - External service error: {}", e.getMessage(), e);
            return ReportedUserCreateResponseDTO.ko(ReportedUserExceptions.SERVICE_UNAVAILABLE);
        }
    }



    @Override
    @Transactional(readOnly = true)
    public List<ReportedUserDTO> searchReportedUser(String userFiscalCode, String merchantId, String initiativeId) {

        log.info("[REPORTED_USER_FIND] - Start finding user");

        String userId = pdvService.encryptCF(userFiscalCode);

        if (userId == null || userId.isEmpty()) {
            return new ArrayList<>();
        }

        log.info("[REPORTED_USER_FIND] - Get userId by fiscalCode");

        boolean alreadyReported = repository.existsByUserId(userId);
        if (!alreadyReported) {
            return new ArrayList<>();
        }
        List<ReportedUser> reportedUsers =  repository.findByUserIdAndInitiativeIdAndMerchantId(userId, initiativeId, merchantId);
        return mapper.toDtoList(reportedUsers, userFiscalCode);

    }

    @Override
    public ReportedUserCreateResponseDTO deleteByUserId(String userFiscalCode, String merchantId, String initiativeId) {

        log.info("[REPORTED_USER_DELETE] - Start delete");

        String userId = pdvService.encryptCF(userFiscalCode);

        if (userId == null || userId.isEmpty()) {
            return ReportedUserCreateResponseDTO.ko(ReportedUserExceptions.USERID_NOT_FOUND);
        }

        log.info("[REPORTED_USER_DELETE] - Get userId by fiscalCode");

        if (repository.existsByUserIdAndInitiativeIdAndMerchantId(userId, initiativeId, merchantId)) {
            repository.deleteByUserIdAndInitiativeIdAndMerchantId(userId, initiativeId, merchantId);
            log.info("[REPORTED_USER_DELETE] - Deleted reported");
            return ReportedUserCreateResponseDTO.ok();
        } else {
            log.info("[REPORTED_USER_DELETE] - User doesn't exists");
            return ReportedUserCreateResponseDTO.ko(ReportedUserExceptions.ENTITY_NOT_FOUND);
        }
    }

    private String sanitizeFiscalCode(String value) {
        if (value == null) return null;
        return value.replace("\"", "");
    }

}
