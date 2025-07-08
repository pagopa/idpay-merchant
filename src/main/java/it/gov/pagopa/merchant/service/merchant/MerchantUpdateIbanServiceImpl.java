package it.gov.pagopa.merchant.service.merchant;

import it.gov.pagopa.merchant.dto.IbanPutDTO;
import it.gov.pagopa.merchant.dto.MerchantDetailDTO;
import it.gov.pagopa.merchant.exception.custom.MerchantNotFoundException;
import it.gov.pagopa.merchant.model.Merchant;
import it.gov.pagopa.merchant.repository.MerchantRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MerchantUpdateIbanServiceImpl implements MerchantUpdateIbanService {

  private final MerchantRepository merchantRepository;
  private final MerchantDetailService merchantDetailService;

  public MerchantUpdateIbanServiceImpl(MerchantRepository merchantRepository,
      MerchantDetailService merchantDetailService) {
    this.merchantRepository = merchantRepository;
    this.merchantDetailService = merchantDetailService;
  }

  @Override
  public MerchantDetailDTO updateIban(String merchantId, String organizationId, String initiativeId, IbanPutDTO ibanPutDTO) {
    Merchant merchant = merchantRepository.findById(merchantId)
        .orElseThrow(() -> new MerchantNotFoundException(
            String.format("Merchant with id %s not found.", merchantId)
        ));

    merchant.getInitiativeList().stream()
        .filter(i -> i.getInitiativeId().equals(initiativeId) && i.getOrganizationId().equals(organizationId))
        .findFirst()
        .orElseThrow(() -> new MerchantNotFoundException(
            String.format("Merchant with id %s is not associated with initiative %s for organization %s.",
                merchantId, initiativeId, organizationId)
        ));

    if (ibanPutDTO.getIban() != null) {
      merchant.setIban(ibanPutDTO.getIban());
    }
    if (ibanPutDTO.getHolder() != null) {
      merchant.setHolder(ibanPutDTO.getHolder());
    }

    merchantRepository.save(merchant);

    return merchantDetailService.getMerchantDetail(organizationId, initiativeId, merchantId);
  }
}
