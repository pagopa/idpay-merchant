package it.gov.pagopa.merchant.controller;

import io.swagger.v3.oas.annotations.Operation;
import it.gov.pagopa.merchant.dto.MerchantListDTO;
import it.gov.pagopa.merchant.dto.MerchantDetailDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/idpay/merchant")
public interface MerchantController {
    @Operation(summary = "Returns the merchants list",
            description = "")
    @GetMapping("/organization/{organizationId}/initiative/{initiativeId}/merchants")
    ResponseEntity<MerchantListDTO> getMerchantList(
            @PathVariable("organizationId") String organizationId,
            @PathVariable("initiativeId") String initiativeId,
            @RequestParam(required = false) String fiscalCode,
            @PageableDefault(size = 15) Pageable pageable
    );

    @Operation(summary = "Returns the merchant detail page on initiative",
            description = "")
    @GetMapping("/{merchantId}/organization/{organizationId}/initiative/{initiativeId}")
    ResponseEntity<MerchantDetailDTO> getMerchantDetail(
            @PathVariable("organizationId") String organizationId,
            @PathVariable("initiativeId") String initiativeId,
            @PathVariable("merchantId") String merchantId
    );
    @Operation(summary = "Returns the merchant",
            description = "")
    @GetMapping("/acquirer/{acquirerId}/merchant-fiscalcode/{fiscalCode}/id")
    @ResponseStatus(code = HttpStatus.OK)
    String retrieveMerchantId(@PathVariable("acquirerId") String acquirerId, @PathVariable("fiscalCode") String fiscalCode);
}