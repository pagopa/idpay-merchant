package it.gov.pagopa.merchant.utils.validator;

import it.gov.pagopa.common.web.dto.MerchantValidationErrorDetail;
import it.gov.pagopa.merchant.exception.custom.MerchantValidationException;
import it.gov.pagopa.merchant.constants.MerchantConstants;
import it.gov.pagopa.merchant.model.Merchant;
import it.gov.pagopa.merchant.service.merchant.MerchantTransactionCheckService;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MerchantValidator {

  private final MerchantTransactionCheckService transactionCheckService;

  public MerchantValidator(MerchantTransactionCheckService transactionCheckService) {
    this.transactionCheckService = transactionCheckService;
  }

  public void validateMerchantWithdrawal(Merchant merchant, String initiativeId) {
    List<MerchantValidationErrorDetail> errors = new ArrayList<>();

    log.debug("[MERCHANT-WITHDRAWAL] Validating withdrawal for merchantId={} and initiativeId={}",
            merchant.getMerchantId(), initiativeId);

    if (merchant.getActivationDate() == null) {
      errors.add(MerchantValidationErrorDetail.builder()
              .code(MerchantConstants.CODE_ACTIVATION_DATE_MISSING)
              .message(MerchantConstants.MSG_ACTIVATION_DATE_MISSING)
              .build());
    } else {
      long days = Duration.between(merchant.getActivationDate(), LocalDateTime.now()).toDays();
      if (days >= 15) {
        log.info("[MERCHANT-WITHDRAWAL] Merchant {} cannot withdraw: {} days have passed since activation",
                merchant.getMerchantId(), days);
        errors.add(MerchantValidationErrorDetail.builder()
                .code(MerchantConstants.CODE_CONTRACT_WITHDRAWAL_TOO_LATE)
                .message(MerchantConstants.MSG_CONTRACT_WITHDRAWAL_TOO_LATE)
                .build());
      }
    }

    boolean hasInProgress = transactionCheckService
            .hasInProgressTransactions(merchant.getMerchantId(), initiativeId);
    if (hasInProgress) {
      errors.add(MerchantValidationErrorDetail.builder()
              .code(MerchantConstants.CODE_TRANSACTIONS_IN_PROGRESS_PRESENT)
              .message(MerchantConstants.MSG_TRANSACTIONS_IN_PROGRESS_PRESENT)
              .build());
    }

    boolean hasProcessed = transactionCheckService
            .hasProcessedTransactions(merchant.getMerchantId(), initiativeId);
    if (hasProcessed) {
      errors.add(MerchantValidationErrorDetail.builder()
              .code(MerchantConstants.CODE_TRANSACTIONS_PROCESSED_PRESENT)
              .message(MerchantConstants.MSG_TRANSACTIONS_PROCESSED_PRESENT)
              .build());
    }

    if (!errors.isEmpty()) {
      Map<String, Long> grouped = errors.stream()
              .collect(Collectors.groupingBy(MerchantValidationErrorDetail::getCode, Collectors.counting()));

      log.warn("[MERCHANT-WITHDRAWAL] Validation failed for merchantId={}: {} errors found",
              merchant.getMerchantId(), errors.size());
      grouped.forEach((code, count) ->
              log.warn("   - {}: {} occurrences", code, count));

      log.debug("[MERCHANT-WITHDRAWAL] Validation error details: {}", errors);
      throw new MerchantValidationException(errors);
    }

    log.info("[MERCHANT-WITHDRAWAL] Validation completed successfully for merchantId={} (eligible for withdrawal)",
            merchant.getMerchantId());
  }
}
