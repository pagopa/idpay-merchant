package it.gov.pagopa.merchant.service;

import it.gov.pagopa.merchant.dto.ReportedUserDTO;
import it.gov.pagopa.merchant.dto.ReportedUserCreateResponseDTO;
import java.util.List;

public interface ReportedUserService {
    ReportedUserCreateResponseDTO createReportedUser(String userFiscalCode, String merchantId, String initiativeId);
    List<ReportedUserDTO> searchReportedUser(String userFiscalCode, String merchantId, String initiativeId);
    ReportedUserCreateResponseDTO deleteByUserId(String userFiscalCode, String merchantId, String initiativeId);
}
