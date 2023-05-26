package it.gov.pagopa.merchant.service.merchant;
import it.gov.pagopa.merchant.dto.*;
import org.springframework.data.domain.Pageable;

public interface MerchantListService {
    MerchantListDTO getMerchantList(String organizationId, String initiativeId, String fiscalCode, Pageable pageable);
}
