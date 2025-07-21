package it.gov.pagopa.merchant.service.merchant;

import it.gov.pagopa.merchant.dto.MerchantIbanPatchDTO;
import it.gov.pagopa.merchant.dto.MerchantDetailDTO;
import it.gov.pagopa.merchant.exception.custom.MerchantNotFoundException;
import it.gov.pagopa.merchant.model.Merchant;
import it.gov.pagopa.merchant.repository.MerchantRepository;
import java.util.Objects;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MerchantUpdateIbanServiceImpl implements MerchantUpdateIbanService {

  private final MerchantRepository merchantRepository;
  private final MerchantDetailService merchantDetailService;

  // Regex for IBAN format
  private static final Pattern ITALIAN_IBAN_PATTERN = Pattern.compile("^IT\\d{2}[A-Z]\\d{5}\\d{5}[A-Z0-9]{12}$");
  // Regex for IBAN Holder format: allows letters (including accented), spaces, apostrophes, and hyphens
  private static final Pattern IBAN_HOLDER_PATTERN = Pattern.compile("^[\\p{L}'\\s-]+$");


  public MerchantUpdateIbanServiceImpl(MerchantRepository merchantRepository,
      MerchantDetailService merchantDetailService) {
    this.merchantRepository = merchantRepository;
    this.merchantDetailService = merchantDetailService;
  }

  @Override
  public MerchantDetailDTO updateIban(String merchantId, String organizationId, String initiativeId, MerchantIbanPatchDTO merchantIbanPatchDTO) {
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

    if (!Objects.isNull(merchantIbanPatchDTO.getIban())) {

      if (!Objects.isNull(merchant.getIban())) {
        throw new IllegalStateException("Invalid state of merchant, IBAN field is not empty");
      }

      if (!ITALIAN_IBAN_PATTERN.matcher(merchantIbanPatchDTO.getIban()).matches()) {
        throw new IllegalArgumentException("Invalid IBAN format.");
      }
      merchant.setIban(merchantIbanPatchDTO.getIban());
    }

    if (!Objects.isNull(merchantIbanPatchDTO.getIbanHolder())) {

      if (!Objects.isNull(merchant.getIbanHolder())) {
        throw new IllegalStateException("Invalid state of merchant, IBAN Holder field is not empty");
      }

      if (!IBAN_HOLDER_PATTERN.matcher(merchantIbanPatchDTO.getIbanHolder()).matches()) {
        throw new IllegalArgumentException("Invalid IBAN holder format.");
      }
      merchant.setIbanHolder(merchantIbanPatchDTO.getIbanHolder());
    }

    merchantRepository.save(merchant);

    return merchantDetailService.getMerchantDetail(organizationId, initiativeId, merchantId);
  }
}