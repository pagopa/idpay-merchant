package it.gov.pagopa.merchant.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import it.gov.pagopa.merchant.dto.ReportedUserDTO;
import it.gov.pagopa.merchant.dto.ReportedUserRequestDTO;
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

    @PostMapping("/reportedUser")
    public ReportedUserCreateResponseDTO create(@Valid @RequestBody ReportedUserRequestDTO dto) {
        return reportedUserService.createReportedUser(dto);
    }

    @GetMapping("/reportedUser")
    public List<ReportedUserDTO> search(
            @RequestParam(required = false) String merchantId,
            @RequestParam(required = false) String initiativeId,
            @RequestParam(required = false) String userFiscalCode
    ) {
        return reportedUserService.searchReportedUser(
                new ReportedUserRequestDTO(merchantId, initiativeId, userFiscalCode));

    }

    @DeleteMapping("/reportedUser")
    public ReportedUserCreateResponseDTO deleteByUser(@RequestParam(required = false) String userFiscalCode) {

        return reportedUserService.deleteByUserId(userFiscalCode);

    }

}
