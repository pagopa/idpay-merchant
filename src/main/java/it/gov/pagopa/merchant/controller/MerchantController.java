package it.gov.pagopa.merchant.controller;

import io.swagger.v3.oas.annotations.Operation;
import it.gov.pagopa.merchant.dto.MerchantIbanPatchDTO;
import it.gov.pagopa.merchant.dto.MerchantDetailDTO;
import it.gov.pagopa.merchant.dto.MerchantListDTO;
import it.gov.pagopa.merchant.dto.MerchantUpdateDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/idpay/merchant")
public interface MerchantController {
    @Operation(summary = "Uploads the merchants file")
    @PutMapping("/organization/{organizationId}/initiative/{initiativeId}/upload")
    @ResponseStatus(code = HttpStatus.OK)
    ResponseEntity<MerchantUpdateDTO> uploadMerchantFile(
            @RequestParam("file") MultipartFile file,
            @PathVariable("organizationId") String organizationId,
            @PathVariable("initiativeId") String initiativeId,
            @RequestHeader("organization-user-id") String organizationUserId);

    @Operation(summary = "Returns the merchants list")
    @GetMapping("/organization/{organizationId}/initiative/{initiativeId}/merchants")
    ResponseEntity<MerchantListDTO> getMerchantList(
            @PathVariable("organizationId") String organizationId,
            @PathVariable("initiativeId") String initiativeId,
            @RequestParam(required = false) String fiscalCode,
            @PageableDefault(size = 15) Pageable pageable
    );

    @Operation(summary = "Returns the merchant detail page on initiative")
    @GetMapping("/{merchantId}/organization/{organizationId}/initiative/{initiativeId}")
    ResponseEntity<MerchantDetailDTO> getMerchantDetail(
            @PathVariable("organizationId") String organizationId,
            @PathVariable("initiativeId") String initiativeId,
            @PathVariable("merchantId") String merchantId
    );

    @Operation(summary = "Patches the iban and/or the holder of a merchant")
    @PatchMapping("/{merchantId}/organization/{organizationId}/initiative/{initiativeId}")
    ResponseEntity<MerchantDetailDTO> updateIban(
        @PathVariable("merchantId") String merchantId,
        @PathVariable("organizationId") String organizationId,
        @PathVariable("initiativeId") String initiativeId,
        @RequestBody MerchantIbanPatchDTO merchantIbanPatchDTO
    );

    @Operation(summary = "Returns the merchant id")
    @GetMapping("/acquirer/{acquirerId}/merchant-fiscalcode/{fiscalCode}/id")
    @ResponseStatus(code = HttpStatus.OK)
    String retrieveMerchantId(@PathVariable("acquirerId") String acquirerId, @PathVariable("fiscalCode") String fiscalCode);
}