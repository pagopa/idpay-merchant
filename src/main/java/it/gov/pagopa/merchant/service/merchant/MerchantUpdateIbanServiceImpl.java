package it.gov.pagopa.merchant.service.merchant;

import it.gov.pagopa.merchant.dto.MerchantIbanPatchDTO;
import it.gov.pagopa.merchant.dto.MerchantDetailDTO;
import it.gov.pagopa.merchant.exception.custom.MerchantBadRequestException;
import it.gov.pagopa.merchant.exception.custom.MerchantNotFoundException;
import it.gov.pagopa.merchant.model.Initiative;
import it.gov.pagopa.merchant.model.Merchant;
import it.gov.pagopa.merchant.repository.MerchantRepository;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static it.gov.pagopa.merchant.utils.Utilities.spaceRemover;

@Service
@Slf4j
public class MerchantUpdateIbanServiceImpl implements MerchantUpdateIbanService {

  private final MerchantRepository merchantRepository;
  private final MerchantDetailService merchantDetailService;

  // Regex for IBAN format
  private static final Pattern ITALIAN_IBAN_PATTERN = Pattern.compile("^IT\\d{2}[A-Z]\\d{5}\\d{5}[A-Z0-9]{12}$");
  // Regex for IBAN Holder format: allows letters (including accented), spaces, apostrophes, and hyphens, and a minimum of 3 ch and a maximum of 70 ch
  private static final Pattern IBAN_HOLDER_PATTERN = Pattern.compile("^.{3,70}$");

  public static final Pattern EMAIL_PATTERN = Pattern.compile("^(?=.{1,255}$)[A-Za-z0-9]([A-Za-z0-9+_-]*(\\.[A-Za-z0-9+_-]+)*)?@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$");


  public MerchantUpdateIbanServiceImpl(MerchantRepository merchantRepository,
      MerchantDetailService merchantDetailService) {
    this.merchantRepository = merchantRepository;
    this.merchantDetailService = merchantDetailService;
  }

  @Override
  public MerchantDetailDTO patchMerchant(String merchantId, String initiativeId, MerchantIbanPatchDTO merchantIbanPatchDTO) {
    Merchant merchant = merchantRepository.findById(merchantId)
        .orElseThrow(() -> new MerchantNotFoundException(
            String.format("Merchant with id %s not found.", merchantId)
        ));

    Optional<Initiative> optionalMerchantInitiative = merchant.getInitiativeList().stream()
        .filter(i -> i.getInitiativeId().equals(initiativeId))
        .findFirst();

    if (optionalMerchantInitiative.isEmpty()){
      throw new MerchantNotFoundException(
              String.format("Merchant with id %s is not associated with initiative %s",
                      merchantId, initiativeId));
    }

    Initiative merchantInitiative = optionalMerchantInitiative.get();

    if (!Objects.isNull(merchantIbanPatchDTO.getIban())) {

      if (!ITALIAN_IBAN_PATTERN.matcher(merchantIbanPatchDTO.getIban()).matches()) {
        throw new MerchantBadRequestException("Invalid IBAN format.");
      }
      merchantInitiative.setIban(merchantIbanPatchDTO.getIban());
    }

    if (!Objects.isNull(merchantIbanPatchDTO.getIbanHolder())) {
      String cleanedHolder = spaceRemover(merchantIbanPatchDTO.getIbanHolder());

      if (!IBAN_HOLDER_PATTERN.matcher(cleanedHolder).matches()) {
        throw new MerchantBadRequestException("Invalid IBAN holder format.");
      }

      merchantInitiative.setIbanHolder(cleanedHolder);
    }

    if (!Objects.isNull(merchantIbanPatchDTO.getOperativeEmail())) {

      if (!EMAIL_PATTERN.matcher(merchantIbanPatchDTO.getOperativeEmail()).matches()) {
        throw new MerchantBadRequestException("Invalid operative email format.");
      }
      merchant.setOperativeEmail(merchantIbanPatchDTO.getOperativeEmail());
    }
    merchant.setUpdateDate(LocalDateTime.now());
    merchantRepository.save(merchant);

    return merchantDetailService.getMerchantDetail(initiativeId, merchantId);
  }
}
