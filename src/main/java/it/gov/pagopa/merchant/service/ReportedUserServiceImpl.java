package it.gov.pagopa.merchant.service;

import it.gov.pagopa.merchant.connector.transaction.TransactionConnector;
import it.gov.pagopa.merchant.constants.ReportedUserExceptions;
import it.gov.pagopa.merchant.dto.ReportedUserRequestDTO;
import it.gov.pagopa.merchant.dto.ReportedUserResponseDTO;
import it.gov.pagopa.merchant.dto.transaction.RewardTransaction;
import it.gov.pagopa.merchant.mapper.ReportedUserMapper;
import it.gov.pagopa.merchant.model.ReportedUser;
import it.gov.pagopa.merchant.repository.ReportedUserRepository;
import it.gov.pagopa.merchant.repository.ReportedUserRepositoryExtended;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import jakarta.annotation.Nullable;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportedUserServiceImpl implements ReportedUserService {

    private static final Logger log = LoggerFactory.getLogger(ReportedUserServiceImpl.class);

    private final ReportedUserRepository repository;
    private final PDVService pdvService;
    @Qualifier("reportedUserRepositoryExtendedImpl")
    private final ReportedUserRepositoryExtended repositoryExt;
    private final ReportedUserMapper mapper;
    private final MongoTemplate mongoTemplate;
    private final TransactionConnector transactionConnector;

    @Override
    public ReportedUserResponseDTO create(ReportedUserRequestDTO dto) {

        log.info("[REPORTED_USER_CREATE] - Start create merchantId={}, fiscalCode={}",
                dto.getMerchantId(), dto.getUserFiscalCode());

        String userId = pdvService.encryptCF(dto.getUserFiscalCode());

        log.info("[REPORTED_USER_CREATE] - Get userId by fiscalCode userId={}", userId);

        if (userId == null || userId.isEmpty()) {
            return ReportedUserResponseDTO.ko(ReportedUserExceptions.USERID_NOT_FOUND);
        }

        boolean alreadyReported = repository.existsByUserId(userId);
        if (alreadyReported) {
            log.info("[REPORTED_USER_CREATE] - User with userId={} already reported", userId);
            return ReportedUserResponseDTO.ko(ReportedUserExceptions.ALREADY_REPORTED);
        }

        try {
            RewardTransaction trx = transactionConnector.findAll(null,
                    userId,
                    LocalDateTime.of(2020, 1, 1, 0, 0),
                    LocalDateTime.of(2030, 1, 1, 0, 0),
                    null,
                    PageRequest.of(0, 10));

            if (trx == null || trx.getInitiatives().isEmpty()) {
                return ReportedUserResponseDTO.ko(ReportedUserExceptions.ENTITY_NOT_FOUND);
            }

            log.info("[REPORTED_USER_CREATE] - Get data by transaction: Initiative = {}", trx.getInitiatives().getFirst());

            if (!Objects.equals(dto.getMerchantId(), trx.getMerchantId())) {
                return ReportedUserResponseDTO.ko(ReportedUserExceptions.DIFFERENT_MERCHANT_ID);
            }

            ReportedUser entity = mapper.toEntity(dto);
            entity.setCreatedAt(LocalDateTime.now());
            entity.setInitiativeId(trx.getInitiatives().getFirst());
            entity.setMerchantId(dto.getMerchantId());
            entity = repository.save(entity);

            log.info("[REPORTED_USER_CREATE] - Created reported user with id={}", entity.getReportedUserId());
            return mapper.toDto(entity);

        } catch (Exception e) {
            log.error("[REPORTED_USER_CREATE] - External service error: {}", e.getMessage(), e);
            return ReportedUserResponseDTO.ko(ReportedUserExceptions.SERVICE_UNAVAILABLE);
        }
    }



    @Override
    @Transactional(readOnly = true)
    public Page<ReportedUserResponseDTO> search(ReportedUserRequestDTO filter, @Nullable Pageable pageable) {


        /*
        log.info("[REPORTED_USER_SEARCH] - Start search merchantId={}, initiativeId={}, userId={}, sort={}",
                filter.getMerchantId(), filter.getInitiativeId(), filter.getUserFiscalCode(),
                pageable != null ? pageable.getSort() : null);

        Criteria c = repositoryExt.getCriteria(filter.getMerchantId(), filter.getInitiativeId(), filter.getUserFiscalCode());



        Pageable effectivePageable = (pageable != null) ? pageable : Pageable.unpaged();

        var list = repositoryExt.findByFilter(c, effectivePageable);
        long total = repositoryExt.getCount(c);

        Page<ReportedUserResponseDTO> page = new PageImpl<>(
                list.stream().map(mapper::toDto).toList(), effectivePageable, total
        );

        log.info("[REPORTED_USER_SEARCH] - Found {} reported users (page {} of size {})",
                page.getTotalElements(), page.getNumber(), page.getSize());

         */
        return null;
    }

    @Override
    public long deleteByUserId(String userId) {
        log.info("[REPORTED_USER_DELETE] - Start delete for userId={}", userId);
        long deleted = repository.deleteByUserId(userId);
        log.info("[REPORTED_USER_DELETE] - Deleted {} reported users for userId={}", deleted, userId);
        return deleted;
    }
}
