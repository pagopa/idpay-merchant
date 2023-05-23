package it.gov.pagopa.merchant.controller;

import io.swagger.v3.oas.annotations.Operation;
import it.gov.pagopa.merchant.dto.MerchantListDTO;
import it.gov.pagopa.merchant.dto.MerchantDetailDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import it.gov.pagopa.merchant.dto.InitiativeDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

@RestController
@RequestMapping("/idpay/merchant")
public interface MerchantController {
    @Operation(summary = "Returns the merchants list",
            description = "")
    @GetMapping("/{initiativeId}")
    ResponseEntity<MerchantListDTO> getMerchantList(
            @PathVariable("initiativeId") String initiativeId,
            @RequestParam(required = false) String fiscalCode,
            @PageableDefault(size = 15) Pageable pageable
    );

    @Operation(summary = "Returns the merchant detail page on initiative",
            description = "")
    @GetMapping("/{initiativeId}/{merchantId}/detail")
    ResponseEntity<MerchantDetailDTO> getMerchantDetail(
            @PathVariable("initiativeId") String initiativeId,
            @PathVariable("merchantId") String merchantId
    );
    @GetMapping("/initiatives")
    @ResponseStatus(code = HttpStatus.OK)
    List<InitiativeDTO> getMerchantInitiativeList(@RequestHeader("x-merchant-id") String merchantId);
}