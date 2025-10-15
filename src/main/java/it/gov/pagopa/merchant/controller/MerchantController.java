package it.gov.pagopa.merchant.controller;

import io.swagger.v3.oas.annotations.Operation;
import it.gov.pagopa.merchant.dto.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/idpay/merchant")
@Validated
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
  String retrieveMerchantId(@PathVariable("acquirerId") String acquirerId,
      @PathVariable("fiscalCode") String fiscalCode);

  @Operation(summary = "Creates a new merchant or retrieves the existing one",
      description = "This endpoint creates a new merchant with the provided details if the merchant does not already exist. " +
          "If a merchant with the given fiscal code already exists, the endpoint returns the internal ID of the existing merchant. " +
          "The request body must include the acquirer ID, business name, and fiscal code.")
  @PutMapping
  ResponseEntity<String> createOrUpdateMerchant(
      @RequestBody @NotNull @Valid MerchantCreateDTO merchantCreateDTO);

  @DeleteMapping("/{merchantId}/deactivate")
  ResponseEntity<Void> deactivateMerchant(@PathVariable String merchantId);
}