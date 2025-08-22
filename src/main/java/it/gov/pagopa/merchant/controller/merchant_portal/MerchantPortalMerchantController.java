package it.gov.pagopa.merchant.controller.merchant_portal;

import io.swagger.v3.oas.annotations.Operation;
import it.gov.pagopa.merchant.dto.InitiativeDTO;
import it.gov.pagopa.merchant.dto.MerchantDetailDTO;
import it.gov.pagopa.merchant.dto.MerchantIbanPatchDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @Operation(summary = "Patches the iban and/or the holder of a merchant")
    @PatchMapping("/initiatives/{initiativeId}")
    ResponseEntity<MerchantDetailDTO> updateIban(
        @RequestHeader("x-merchant-id") String merchantId,
        @PathVariable("initiativeId") String initiativeId,
        @RequestBody MerchantIbanPatchDTO merchantIbanPatchDTO
    );
}
