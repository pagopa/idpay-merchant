package it.gov.pagopa.merchant.service;

import it.gov.pagopa.merchant.dto.ReportedUserRequestDTO;
import it.gov.pagopa.merchant.dto.ReportedUserResponseDTO;
import it.gov.pagopa.merchant.mapper.ReportedUserMapper;
import it.gov.pagopa.merchant.model.Initiative;
import it.gov.pagopa.merchant.model.Merchant;
import it.gov.pagopa.merchant.model.ReportedUser;
import it.gov.pagopa.merchant.repository.ReportedUserRepository;
import it.gov.pagopa.merchant.repository.ReportedUserRepositoryExtended;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import jakarta.annotation.Nullable;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportedUserServiceImpl implements ReportedUserService {

    private static final Logger log = LoggerFactory.getLogger(ReportedUserServiceImpl.class);

    private final ReportedUserRepository repository;
    @Qualifier("reportedUserRepositoryExtendedImpl")
    private final ReportedUserRepositoryExtended repositoryExt;
    private final ReportedUserMapper mapper;
    private final MongoTemplate mongoTemplate;

    @Override
    public ReportedUserResponseDTO create(ReportedUserRequestDTO dto) {
        log.info("[REPORTED_USER_CREATE] - Start create merchantId={}, initiativeId={}, userId={}",
                dto.getMerchantId(), dto.getInitiativeId(), dto.getUserId());

        if (!isInitiativeJoinedByMerchant(dto.getMerchantId(), dto.getInitiativeId())) {
            log.warn("[REPORTED_USER_CREATE] - Initiative {} not joined by merchant {}", dto.getInitiativeId(), dto.getMerchantId());
            throw new NotFoundException("InitiativeId not found for this merchant");
        }

        ReportedUser entity = mapper.toEntity(dto);
        entity.setCreatedAt(LocalDateTime.now());
        entity = repository.save(entity);
        log.info("[REPORTED_USER_CREATE] - Created reported user with id={}", entity.getReportedUserId());
        return mapper.toDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReportedUserResponseDTO> search(ReportedUserRequestDTO filter, @Nullable Pageable pageable) {
        log.info("[REPORTED_USER_SEARCH] - Start search merchantId={}, initiativeId={}, userId={}, sort={}",
                filter.getMerchantId(), filter.getInitiativeId(), filter.getUserId(),
                pageable != null ? pageable.getSort() : null);

        Criteria c = repositoryExt.getCriteria(filter.getMerchantId(), filter.getInitiativeId(), filter.getUserId());

        Pageable effectivePageable = (pageable != null) ? pageable : Pageable.unpaged();

        var list = repositoryExt.findByFilter(c, effectivePageable);
        long total = repositoryExt.getCount(c);

        Page<ReportedUserResponseDTO> page = new PageImpl<>(
                list.stream().map(mapper::toDto).toList(), effectivePageable, total
        );

        log.info("[REPORTED_USER_SEARCH] - Found {} reported users (page {} of size {})",
                page.getTotalElements(), page.getNumber(), page.getSize());
        return page;
    }

    @Override
    public long deleteByUserId(String userId) {
        log.info("[REPORTED_USER_DELETE] - Start delete for userId={}", userId);
        long deleted = repository.deleteByUserId(userId);
        log.info("[REPORTED_USER_DELETE] - Deleted {} reported users for userId={}", deleted, userId);
        return deleted;
    }

    private boolean isInitiativeJoinedByMerchant(String merchantId, String initiativeId) {
        log.info("[REPORTED_USER_CHECK_JOIN] - Checking if merchantId={} joined initiativeId={}", merchantId, initiativeId);
        var q = new Query(
                Criteria
                        .where(Merchant.Fields.merchantId).is(merchantId)
                        .and(Merchant.Fields.initiativeList + "." + Initiative.Fields.initiativeId).is(initiativeId)
        );
        q.fields().include(Merchant.Fields.merchantId);
        boolean exists = mongoTemplate.exists(q, Merchant.class);
        log.info("[REPORTED_USER_CHECK_JOIN] - MerchantId={} joined initiativeId={} -> {}", merchantId, initiativeId, exists);
        return exists;
    }
}
