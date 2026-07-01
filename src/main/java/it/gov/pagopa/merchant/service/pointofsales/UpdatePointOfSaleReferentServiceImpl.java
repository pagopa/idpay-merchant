package it.gov.pagopa.merchant.service.pointofsales;

import it.gov.pagopa.merchant.constants.PointOfSaleConstants;
import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleReferentPatchDTO;
import it.gov.pagopa.merchant.exception.custom.PointOfSaleDuplicateException;
import it.gov.pagopa.merchant.model.PointOfSale;
import it.gov.pagopa.merchant.repository.PointOfSaleRepository;
import it.gov.pagopa.merchant.service.KeycloakService;
import it.gov.pagopa.merchant.utils.validator.PointOfSaleValidator;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UpdatePointOfSaleReferentServiceImpl implements UpdatePointOfSaleReferentService {

  private final PointOfSaleRepository pointOfSaleRepository;
  private final GetPointOfSaleService getPointOfSaleService;
  private final KeycloakService keycloakService;
  private final PointOfSaleValidator pointOfSaleValidator;

  @Override
  public PointOfSale updateReferent(String merchantId, String pointOfSaleId,
      PointOfSaleReferentPatchDTO referentPatchDTO) {
    PointOfSale pointOfSale = getPointOfSaleService.getPointOfSaleByIdAndMerchantId(
        pointOfSaleId, merchantId);

    String oldEmail = pointOfSale.getContactEmail();
    String newEmail = normalizeEmail(referentPatchDTO.getContactEmail());

    pointOfSaleValidator.validateEmailFormat(newEmail);
    validateEmailUniqueness(pointOfSaleId, newEmail);

    pointOfSale.setContactEmail(newEmail);
    pointOfSale.setContactName(StringUtils.trim(referentPatchDTO.getContactName()));
    pointOfSale.setContactSurname(StringUtils.trim(referentPatchDTO.getContactSurname()));

    PointOfSale updatedPointOfSale = pointOfSaleRepository.save(pointOfSale);

    boolean emailChanged = !StringUtils.equalsIgnoreCase(oldEmail, newEmail);
    keycloakService.updateReferentUserOnKeycloak(updatedPointOfSale, oldEmail, emailChanged);

    return updatedPointOfSale;
  }

  private String normalizeEmail(String email) {
    return StringUtils.trim(email).toLowerCase(Locale.ROOT);
  }

  private void validateEmailUniqueness(String pointOfSaleId, String newEmail) {
    pointOfSaleRepository.findByContactEmail(newEmail)
        .filter(existing -> !pointOfSaleId.equals(existing.getId()))
        .ifPresent(existing -> {
          throw new PointOfSaleDuplicateException(PointOfSaleConstants.MSG_ALREADY_REGISTERED);
        });
  }
}
