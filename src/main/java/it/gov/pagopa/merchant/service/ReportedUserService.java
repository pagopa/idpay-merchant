package it.gov.pagopa.merchant.service;

import it.gov.pagopa.merchant.dto.ReportedUserDTO;
import it.gov.pagopa.merchant.dto.ReportedUserCreateResponseDTO;
import java.util.List;

public interface ReportedUserService {
    ReportedUserCreateResponseDTO createReportedUser(String userId, String merchantId, String initiativeId);
    List<ReportedUserDTO> searchReportedUser(String userId, String merchantId, String initiativeId);
    ReportedUserCreateResponseDTO deleteByUserId(String userId, String merchantId, String initiativeId);
}
