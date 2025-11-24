package it.gov.pagopa.merchant.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import it.gov.pagopa.common.web.dto.ErrorDTO;
import it.gov.pagopa.common.web.dto.ValidationErrorDTO;
import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleDTO;
import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleDetailDTO;
import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleListDTO;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/idpay/merchant/portal")
@Validated
public interface PointOfSaleController {


    @Operation(
            summary = "Save or update a list of points of sale",
            security = {@SecurityRequirement(name = "Bearer")},
            tags = {"point-of-sales"},
            description = "Save or update a list of points of sale fo the specified merchant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content - Successfully saved or updated"),
            @ApiResponse(responseCode = "400", description = "Bad request - Invalid input data", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationErrorDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication failed", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "429", description = "Too many Request - Rate limit exceeded", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class)))})
    @PutMapping(value = "/{merchantId}/point-of-sales")
    ResponseEntity<Void> savePointOfSales(
            @PathVariable("merchantId") @NotBlank String merchantId,
            @RequestBody List<PointOfSaleDTO> pointOfSales);

    @Operation(
            summary = "Retrieve a list of points of sale",
            security = {@SecurityRequirement(name = "Bearer")},
            tags = {"point-of-sales"},
            description = "Returns a paginated list of points of sale for the specified merchant, with optional filters.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful response with a paginated list of points of sale" , content = @Content(mediaType = "application/json", schema = @Schema(implementation = PointOfSaleListDTO.class))),
            @ApiResponse(responseCode = "400", description = "Bad request - Invalid input data", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication failed", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "429", description = "Too many Request - Rate limit exceeded", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class)))})
    @GetMapping(value = "/{merchantId}/point-of-sales")
    ResponseEntity<PointOfSaleListDTO> getPointOfSalesList(
            @PathVariable("merchantId") String merchantId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String contactName,
            @PageableDefault(size = 8) Pageable pageable);

    @Operation(
            summary = "Retrieve point of sale detail",
            security = {@SecurityRequirement(name = "Bearer")},
            tags = {"point-of-sales"},
            description = "Returns the detail of a point of sale for this merchant"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful response with point of sale detail",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PointOfSaleDetailDTO.class))),
            @ApiResponse(responseCode = "400", description = "Bad request - Invalid input data",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication failed",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "404", description = "Point of sale or merchant not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "429", description = "Too many Requests - Rate limit exceeded",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class)))
    })
    @GetMapping("/{merchantId}/point-of-sales/{pointOfSaleId}")
    ResponseEntity<PointOfSaleDTO> getPointOfSale(
            @PathVariable("pointOfSaleId") String pointOfSaleId,
            @PathVariable("merchantId") String merchantId,
            @RequestHeader("x-point-of-sale-id") String tokenPointOfSaleId
    );
}
