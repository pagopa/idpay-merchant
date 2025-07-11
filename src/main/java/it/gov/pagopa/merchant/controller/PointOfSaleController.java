package it.gov.pagopa.merchant.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import it.gov.pagopa.common.web.dto.ErrorDTO;
import it.gov.pagopa.merchant.dto.point_of_sales.PointOfSaleDTO;
import it.gov.pagopa.merchant.utils.validator.ValidationApiEnabledGroup;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/idpay/merchant")
@Validated
public interface PointOfSaleController {

    @Operation(
            summary = "Save the sale list of the merchant",
            security = {@SecurityRequirement(name = "Bearer")},
            tags = {"Merchant Point of Sales"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "400", description = "Bad request - Invalid input data", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication failed", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "429", description = "Too many Request - Rate limit exceeded", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class)))})
    @PutMapping(value = "/{merchantId}/point-of-sales")
    ResponseEntity<Void> savePointOfSales(
            @PathVariable("merchantId") @NotBlank String merchantId,
            @RequestBody @Validated(ValidationApiEnabledGroup.class) List<@Valid PointOfSaleDTO> pointOfSales);


}