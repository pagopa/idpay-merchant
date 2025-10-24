package it.gov.pagopa.merchant.service;

import it.gov.pagopa.merchant.dto.ReportedUserRequestDTO;
import it.gov.pagopa.merchant.dto.ReportedUserResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReportedUserService {
    ReportedUserResponseDTO create(ReportedUserRequestDTO dto);
    Page<ReportedUserResponseDTO> search(ReportedUserRequestDTO filter, Pageable pageable);
    long deleteByUserId(String userId);
}
