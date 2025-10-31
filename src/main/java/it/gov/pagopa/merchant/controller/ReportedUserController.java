package it.gov.pagopa.merchant.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import it.gov.pagopa.merchant.dto.ReportedUserDTO;
import it.gov.pagopa.merchant.dto.ReportedUserCreateResponseDTO;
import it.gov.pagopa.merchant.service.ReportedUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/idpay/merchant")
@RequiredArgsConstructor
@Validated
@Tag(name = "Reported Users", description = "Gestione segnalazioni utenti per iniziativa/merchant")
public class ReportedUserController {

    private static final Logger log = LoggerFactory.getLogger(ReportedUserController.class);

    private final ReportedUserService reportedUserService;

    @PostMapping("/reported-user")
    public ReportedUserCreateResponseDTO create(@Valid @RequestBody String userFiscalCode,
                                                @RequestHeader ("merchant-id") String merchantId,
                                                @RequestHeader ("initiative-id")String initiativeId) {
        return reportedUserService.createReportedUser(userFiscalCode, merchantId, initiativeId);
    }

    @GetMapping("/reported-user")
    public List<ReportedUserDTO> getReportedUser(
            @RequestParam (required = true) String userFiscalCode,
            @RequestHeader ("merchant-id") String merchantId,
            @RequestHeader ("initiative-id")String initiativeId
    ) {
        return reportedUserService.searchReportedUser(userFiscalCode, merchantId, initiativeId);

    }

    @DeleteMapping("/reported-user/{userFiscalCode}")
    public ReportedUserCreateResponseDTO deleteByUser(@PathVariable String userFiscalCode,
                                                      @RequestHeader ("merchant-id") String merchantId,
                                                      @RequestHeader ("initiative-id")String initiativeId) {

        return reportedUserService.deleteByUserId(userFiscalCode, merchantId, initiativeId);

    }

}
