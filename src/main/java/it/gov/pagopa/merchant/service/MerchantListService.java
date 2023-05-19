package it.gov.pagopa.merchant.service;
import it.gov.pagopa.merchant.dto.*;
import org.springframework.data.domain.Pageable;

public interface MerchantListService {
    MerchantListDTO getMerchantList(String initiativeId, String fiscalCode, Pageable pageable);
}
