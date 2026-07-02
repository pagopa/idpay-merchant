package it.gov.pagopa.merchant.service.pointofsales;

import it.gov.pagopa.common.web.exception.ClientExceptionWithBody;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UpdatePointOfSaleReferentServiceImpl implements UpdatePointOfSaleReferentService {

  private final PointOfSaleRepository pointOfSaleRepository;
  private final PointOfSaleFinderService pointOfSaleFinderService;
  private final KeycloakService keycloakService;
  private final PointOfSaleValidator pointOfSaleValidator;

  @Override
  public PointOfSale updateReferent(String merchantId, String pointOfSaleId,
      PointOfSaleReferentPatchDTO referentPatchDTO) {
    PointOfSale pointOfSale = pointOfSaleFinderService.getPointOfSaleByIdAndMerchantId(
        pointOfSaleId, merchantId);

    String oldEmail = pointOfSale.getContactEmail();
    String oldName = pointOfSale.getContactName();
    String oldSurname = pointOfSale.getContactSurname();
    boolean emailProvided = referentPatchDTO.getContactEmail() != null;
    String newEmail = resolveEmail(oldEmail, referentPatchDTO.getContactEmail());
    String newName = resolveReferentField(oldName, referentPatchDTO.getContactName(), "contactName");
    String newSurname = resolveReferentField(oldSurname, referentPatchDTO.getContactSurname(),
        "contactSurname");

    if (emailProvided) {
      pointOfSaleValidator.validateEmailFormat(newEmail);
      validateEmailUniqueness(pointOfSaleId, newEmail);
    }

    pointOfSale.setContactEmail(newEmail);
    pointOfSale.setContactName(newName);
    pointOfSale.setContactSurname(newSurname);

    PointOfSale updatedPointOfSale = pointOfSaleRepository.save(pointOfSale);

    boolean emailChanged = !StringUtils.equalsIgnoreCase(oldEmail, newEmail);
    boolean referentChanged = emailChanged
        || !StringUtils.equals(oldName, newName)
        || !StringUtils.equals(oldSurname, newSurname);
    if (referentChanged) {
      keycloakService.updateReferentUserOnKeycloak(updatedPointOfSale, oldEmail, emailChanged);
    }

    return updatedPointOfSale;
  }

  private String normalizeEmail(String email) {
    return StringUtils.trim(email).toLowerCase(Locale.ROOT);
  }

  private String resolveEmail(String oldEmail, String requestedEmail) {
    if (requestedEmail == null) {
      return oldEmail;
    }

    if (StringUtils.isBlank(requestedEmail)) {
      throw new ClientExceptionWithBody(
          HttpStatus.BAD_REQUEST,
          PointOfSaleConstants.CODE_INVALID_EMAIL,
          PointOfSaleConstants.MSG_INVALID_EMAIL);
    }

    return normalizeEmail(requestedEmail);
  }

  private String resolveReferentField(String oldValue, String requestedValue, String fieldName) {
    if (requestedValue == null) {
      return oldValue;
    }

    if (StringUtils.isBlank(requestedValue)) {
      throw new ClientExceptionWithBody(
          HttpStatus.BAD_REQUEST,
          PointOfSaleConstants.CODE_FIELD_REQUIRED,
          "[%s]: must not be blank".formatted(fieldName));
    }

    return StringUtils.trim(requestedValue);
  }

  private void validateEmailUniqueness(String pointOfSaleId, String newEmail) {
    pointOfSaleRepository.findByContactEmail(newEmail)
        .filter(existing -> !pointOfSaleId.equals(existing.getId()))
        .ifPresent(existing -> {
          throw new PointOfSaleDuplicateException(PointOfSaleConstants.MSG_ALREADY_REGISTERED);
        });
  }
}
