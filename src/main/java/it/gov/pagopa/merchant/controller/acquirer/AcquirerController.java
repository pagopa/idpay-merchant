package it.gov.pagopa.merchant.controller.acquirer;

import io.swagger.v3.oas.annotations.Operation;
import it.gov.pagopa.merchant.dto.InitiativeDTO;
import it.gov.pagopa.merchant.dto.MerchantUpdateDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/idpay/merchant/acquirer")
public interface AcquirerController {

    @Operation(summary = "Returns the list of initiatives of a specific merchant")
    @GetMapping("/initiatives")
    @ResponseStatus(code = HttpStatus.OK)
    List<InitiativeDTO> getMerchantInitiativeList(@RequestHeader("x-merchant-id") String merchantId);

    @Operation(summary = "Uploads the merchants file")
    @PutMapping("/{acquirerId}/initiative/{initiativeId}/upload")
    @ResponseStatus(code = HttpStatus.OK)
    ResponseEntity<MerchantUpdateDTO> uploadMerchantFile(
            @RequestParam("file") MultipartFile file,
            @PathVariable("acquirerId") String acquirerId,
            @PathVariable("initiativeId") String initiativeId);
}
