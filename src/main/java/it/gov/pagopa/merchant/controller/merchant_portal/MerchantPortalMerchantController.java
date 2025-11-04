package it.gov.pagopa.merchant.controller.merchant_portal;

import io.swagger.v3.oas.annotations.Operation;
import it.gov.pagopa.merchant.dto.InitiativeDTO;
import it.gov.pagopa.merchant.dto.MerchantDetailDTO;
import it.gov.pagopa.merchant.dto.ReportedUserCreateResponseDTO;
import it.gov.pagopa.merchant.dto.ReportedUserDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/idpay/merchant/portal")
public interface MerchantPortalMerchantController {

    @Operation(summary = "Returns the list of initiatives of a specific merchant")
    @GetMapping("/initiatives")
    @ResponseStatus(code = HttpStatus.OK)
    List<InitiativeDTO> getMerchantInitiativeList(@RequestHeader("x-merchant-id") String merchantId);

    @Operation(summary = "Returns the merchant detail")
    @GetMapping("/initiatives/{initiativeId}")
    MerchantDetailDTO getMerchantDetail(
            @RequestHeader("x-merchant-id") String merchantId,
            @PathVariable("initiativeId") String initiativeId
    );

    @Operation(summary = "Create a new reported user")
    @PostMapping("/reported-user")
    ReportedUserCreateResponseDTO create(@Valid @RequestBody String userFiscalCode,
                                                @RequestHeader ("x-merchant-id") String merchantId,
                                                @RequestHeader ("initiative-id")String initiativeId);

    @Operation(summary = "Returns the reported user")
    @GetMapping("/reported-user")
    List<ReportedUserDTO> getReportedUser(
            @RequestParam (required = true) String userFiscalCode,
            @RequestHeader ("x-merchant-id") String merchantId,
            @RequestHeader ("initiative-id")String initiativeId);

    @Operation(summary = "Delete the reported user")
    @DeleteMapping("/reported-user/{userFiscalCode}")
    ReportedUserCreateResponseDTO deleteByUser(@PathVariable String userFiscalCode,
                                                      @RequestHeader ("x-merchant-id") String merchantId,
                                                      @RequestHeader ("initiative-id")String initiativeId);
}
