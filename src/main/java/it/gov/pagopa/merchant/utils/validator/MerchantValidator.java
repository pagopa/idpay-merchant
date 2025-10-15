package it.gov.pagopa.merchant.utils.validator;

import it.gov.pagopa.common.web.dto.MerchantValidationErrorDetail;
import it.gov.pagopa.common.web.exception.ClientExceptionWithBody;
import it.gov.pagopa.common.web.exception.MerchantValidationException;
import it.gov.pagopa.merchant.constants.MerchantConstants;
import it.gov.pagopa.merchant.model.Initiative;
import it.gov.pagopa.merchant.model.Merchant;
import it.gov.pagopa.merchant.model.PointOfSale;
import it.gov.pagopa.merchant.service.pointofsales.PointOfSaleTransactionCheckService;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class MerchantValidator {

  private final PointOfSaleTransactionCheckService pointOfSaleTransactionCheckService;

  public MerchantValidator(
      PointOfSaleTransactionCheckService pointOfSaleTransactionCheckService) {
    this.pointOfSaleTransactionCheckService = pointOfSaleTransactionCheckService;
  }

  public void validateMerchantWithdrawal(Merchant merchant, List<PointOfSale> points) {
    List<MerchantValidationErrorDetail> errors = new ArrayList<>();

    if (merchant.getActivationDate() != null) {
      long days = Duration.between(merchant.getActivationDate(), LocalDateTime.now()).toDays();
      if (days >= 15) {
        errors.add(MerchantValidationErrorDetail.builder()
            .code(MerchantConstants.CODE_CONTRACT_WITHDRAWAL_TOO_LATE)
            .message(MerchantConstants.MSG_CONTRACT_WITHDRAWAL_TOO_LATE)
            .build());
      }
    }

    List<String> posIds = points.stream()
        .map(PointOfSale::getId)
        .toList();

    Initiative bonusInitiative = findMerchantInitiative(merchant);

    boolean hasInProgress = pointOfSaleTransactionCheckService
        .hasInProgressTransactions(merchant.getMerchantId(), bonusInitiative.getInitiativeId(), posIds);

    if (hasInProgress) {
      errors.add(MerchantValidationErrorDetail.builder()
          .code(MerchantConstants.CODE_TRANSACTIONS_PRESENT)
          .message(MerchantConstants.MSG_TRANSACTIONS_PRESENT)
          .build());
    } else {
      boolean hasProcessed = Boolean.TRUE.equals(pointOfSaleTransactionCheckService
          .hasProcessedTransactions(merchant.getMerchantId(), bonusInitiative.getInitiativeId(),
              posIds)
          .block(Duration.ofSeconds(5)));

      if (hasProcessed) {
        errors.add(MerchantValidationErrorDetail.builder()
            .code(MerchantConstants.CODE_TRANSACTIONS_PRESENT)
            .message(MerchantConstants.MSG_TRANSACTIONS_PRESENT)
            .build());
      }
    }

    if (!errors.isEmpty()) {
      throw new MerchantValidationException(errors);
    }
  }

  private Initiative findMerchantInitiative(Merchant merchant) {
    return merchant.getInitiativeList().stream()
        .filter(initiative -> initiative.isEnabled()
            && "Bonus Elettrodomestici".equalsIgnoreCase(initiative.getInitiativeName()))
        .findFirst()
        .orElseThrow(() -> new ClientExceptionWithBody(
            HttpStatus.BAD_REQUEST,
            MerchantConstants.CODE_MERCHANT_WITHDRAWAL_ERROR,
            String.format("Initiative not found for merchant %s", merchant.getMerchantId())
        ));
  }
}
