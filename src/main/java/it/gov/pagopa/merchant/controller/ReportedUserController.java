package it.gov.pagopa.merchant.controller;

import it.gov.pagopa.merchant.dto.ReportedUserRequestDTO;
import it.gov.pagopa.merchant.dto.ReportedUserResponseDTO;
import it.gov.pagopa.merchant.service.ReportedUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reported-users")
@RequiredArgsConstructor
@Validated
public class ReportedUserController {

    private static final Logger log = LoggerFactory.getLogger(ReportedUserController.class);

    private final ReportedUserService service;

    @PostMapping
    public ReportedUserResponseDTO create(@Valid @RequestBody ReportedUserRequestDTO dto) {
        log.info("[REPORTED_USER_CREATE] - Received create request merchantId={}, initiativeId={}, userId={}",
                dto.getMerchantId(), dto.getInitiativeId(), dto.getUserId());
        ReportedUserResponseDTO response = service.create(dto);
        log.info("[REPORTED_USER_CREATE] - Created reported user for userId={} in initiativeId={} (merchantId={})",
                dto.getUserId(), dto.getInitiativeId(), dto.getMerchantId());
        return response;
    }

    @GetMapping
    public Page<ReportedUserResponseDTO> search(
            @RequestParam(required = false) String merchantId,
            @RequestParam(required = false) String initiativeId,
            @RequestParam(required = false) String userId,
            Pageable pageable
    ) {
        log.info("[REPORTED_USER_SEARCH] - Received search request merchantId={}, initiativeId={}, userId={}, sort={}",
                merchantId, initiativeId, userId, pageable != null ? pageable.getSort() : null);
        Page<ReportedUserResponseDTO> result =
                service.search(new ReportedUserRequestDTO(merchantId, initiativeId, userId), pageable);
        log.info("[REPORTED_USER_SEARCH] - Returning {} reported users (page {} of size {})",
                result.getTotalElements(), result.getNumber(), result.getSize());
        return result;
    }

    @DeleteMapping("/by-user/{userId}")
    public long deleteByUser(@PathVariable String userId) {
        log.info("[REPORTED_USER_DELETE] - Received delete request for userId={}", userId);
        long deleted = service.deleteByUserId(userId);
        log.info("[REPORTED_USER_DELETE] - Deleted {} reported users for userId={}", deleted, userId);
        return deleted;
    }
}
