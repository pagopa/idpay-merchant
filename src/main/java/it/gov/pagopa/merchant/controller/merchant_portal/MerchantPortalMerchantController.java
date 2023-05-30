package it.gov.pagopa.merchant.controller.merchant_portal;

import io.swagger.v3.oas.annotations.Operation;
import it.gov.pagopa.merchant.dto.InitiativeDTO;
import it.gov.pagopa.merchant.dto.MerchantDetailDTO;
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
    @GetMapping("/initiative/{initiativeId}")
    MerchantDetailDTO getMerchantDetail(
            @RequestHeader("x-merchant-id") String merchantId,
            @PathVariable("initiativeId") String initiativeId
    );
}
