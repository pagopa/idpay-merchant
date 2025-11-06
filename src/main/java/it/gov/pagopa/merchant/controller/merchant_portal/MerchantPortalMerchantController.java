package it.gov.pagopa.merchant.controller.merchant_portal;

import io.swagger.v3.oas.annotations.Operation;
import it.gov.pagopa.merchant.dto.InitiativeDTO;
import it.gov.pagopa.merchant.dto.MerchantDetailDTO;
import it.gov.pagopa.merchant.dto.ReportedUserCreateResponseDTO;
import it.gov.pagopa.merchant.dto.ReportedUserDTO;
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
    @PostMapping("/reported-user/{userId}")
    ReportedUserCreateResponseDTO createReportedUser(@RequestHeader ("x-merchant-id")String merchantId,
                                                     @RequestHeader ("initiative-id")String initiativeId,
                                                     @PathVariable String userId);

    @Operation(summary = "Returns the reported user")
    @GetMapping("/reported-user/{userId}")
    List<ReportedUserDTO> getReportedUser(
            @RequestHeader ("x-merchant-id")String merchantId,
            @RequestHeader ("initiative-id")String initiativeId,
            @PathVariable String userId);

    @Operation(summary = "Delete the reported user")
    @DeleteMapping("/reported-user/{userId}")
    ReportedUserCreateResponseDTO deleteReportedUser(@RequestHeader ("x-merchant-id")String merchantId,
                                                     @RequestHeader ("initiative-id")String initiativeId,
                                                     @PathVariable String userId);
}
