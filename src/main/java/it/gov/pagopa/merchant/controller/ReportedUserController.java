package it.gov.pagopa.merchant.controller;

import it.gov.pagopa.merchant.dto.ReportedUserRequestDTO;
import it.gov.pagopa.merchant.dto.ReportedUserResponseDTO;
import it.gov.pagopa.merchant.service.ReportedUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reported-users")
@RequiredArgsConstructor
@Validated
public class ReportedUserController {
    private final ReportedUserService service;

    @PostMapping
    public ReportedUserResponseDTO create(@Valid @RequestBody ReportedUserRequestDTO dto) {
        return service.create(dto);
    }


    @GetMapping
    public Page<ReportedUserResponseDTO> search(
            @RequestParam(required = false) String merchantId,
            @RequestParam(required = false) String initiativeId,
            @RequestParam(required = false) String userId,
            Pageable pageable
    ) {
        return service.search(new ReportedUserRequestDTO(merchantId, initiativeId, userId), pageable);
    }


    @DeleteMapping("/by-user/{userId}")
    public long deleteByUser(@PathVariable String userId) {
        return service.deleteByUserId(userId);
    }
}
