package it.gov.pagopa.merchant.service;

import it.gov.pagopa.merchant.connector.transaction.TransactionConnector;
import it.gov.pagopa.merchant.constants.ReportedUserExceptions;
import it.gov.pagopa.merchant.dto.ReportedUserDTO;
import it.gov.pagopa.merchant.dto.ReportedUserRequestDTO;
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
import java.util.Objects;

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
    public ReportedUserCreateResponseDTO createReportedUser(ReportedUserRequestDTO dto) {

        log.info("[REPORTED_USER_CREATE] - Start create merchantId={}, fiscalCode={}, initiativeId={}",
                dto.getMerchantId(), dto.getUserFiscalCode(), dto.getInitiativeId());

        String userId = pdvService.encryptCF(dto.getUserFiscalCode());

        if (userId == null || userId.isEmpty()) {
            return ReportedUserCreateResponseDTO.ko(ReportedUserExceptions.USERID_NOT_FOUND);
        }

        log.info("[REPORTED_USER_CREATE] - Get userId by fiscalCode userId={}", userId);

        boolean alreadyReported = repository.existsByUserId(userId);
        if (alreadyReported) {
            log.info("[REPORTED_USER_CREATE] - User with userId={} already reported", userId);
            return ReportedUserCreateResponseDTO.ko(ReportedUserExceptions.ALREADY_REPORTED);
        }

        try {
            RewardTransaction trx = transactionConnector.findAll(null,
                    userId,
                    LocalDateTime.of(2020, 1, 1, 0, 0),
                    LocalDateTime.of(2030, 1, 1, 0, 0),
                    null,
                    PageRequest.of(0, 10));

            if (trx == null || trx.getInitiatives().isEmpty()) {
                return ReportedUserCreateResponseDTO.ko(ReportedUserExceptions.ENTITY_NOT_FOUND);
            }

            log.info("[REPORTED_USER_CREATE] - Get data by transaction: Initiative = {}", trx.getInitiatives().getFirst());

            if (!Objects.equals(dto.getMerchantId(), trx.getMerchantId())) {
                return ReportedUserCreateResponseDTO.ko(ReportedUserExceptions.DIFFERENT_MERCHANT_ID);
            }
            if (!Objects.equals(dto.getInitiativeId(), trx.getInitiatives().getFirst())) {
                return ReportedUserCreateResponseDTO.ko(ReportedUserExceptions.DIFFERENT_INITIATIVE_ID);
            }

            ReportedUser entity = mapper.fromRequestDtoToEntity(dto);
            entity.setUserId(userId);
            entity.setCreatedAt(LocalDateTime.now());
            entity.setInitiativeId(trx.getInitiatives().getFirst());
            entity.setMerchantId(dto.getMerchantId());
            entity = repository.save(entity);

            log.info("[REPORTED_USER_CREATE] - Created reported user with id={}", entity.getReportedUserId());
            return ReportedUserCreateResponseDTO.ok();

        } catch (Exception e) {
            log.error("[REPORTED_USER_CREATE] - External service error: {}", e.getMessage(), e);
            return ReportedUserCreateResponseDTO.ko(ReportedUserExceptions.SERVICE_UNAVAILABLE);
        }
    }



    @Override
    @Transactional(readOnly = true)
    public List<ReportedUserDTO> searchReportedUser(ReportedUserRequestDTO dto) {

        log.info("[REPORTED_USER_FIND] - Start finding user merchantId={}, fiscalCode={}, initiativeId={}",
                dto.getMerchantId(), dto.getUserFiscalCode(), dto.getInitiativeId());

        String userId = pdvService.encryptCF(dto.getUserFiscalCode());

        if (userId == null || userId.isEmpty()) {

            return new ArrayList<>();
        }

        log.info("[REPORTED_USER_FIND] - Get userId by fiscalCode userId={}", userId);

        boolean alreadyReported = repository.existsByUserId(userId);
        if (!alreadyReported) {
            return new ArrayList<>();
        }
        List<ReportedUser>  entities =  repository.findByUserId(userId);
        return mapper.toDtoList(entities, dto.getUserFiscalCode());

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

    }

    @Override
    public long deleteByUserId(String userId) {
        log.info("[REPORTED_USER_DELETE] - Start delete for userId={}", userId);
        long deleted = repository.deleteByUserId(userId);
        log.info("[REPORTED_USER_DELETE] - Deleted {} reported users for userId={}", deleted, userId);
        return deleted;
    }
}
