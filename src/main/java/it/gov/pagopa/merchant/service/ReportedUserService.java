package it.gov.pagopa.merchant.service;

import it.gov.pagopa.merchant.dto.ReportedUserDTO;
import it.gov.pagopa.merchant.dto.ReportedUserRequestDTO;
import it.gov.pagopa.merchant.dto.ReportedUserCreateResponseDTO;
import java.util.List;

public interface ReportedUserService {
    ReportedUserCreateResponseDTO createReportedUser(ReportedUserRequestDTO dto);
    List<ReportedUserDTO> searchReportedUser(ReportedUserRequestDTO filter);
    ReportedUserCreateResponseDTO deleteByUserId(String userFiscalCode);
}
