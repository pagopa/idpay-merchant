package it.gov.pagopa.merchant.utils.validator;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import it.gov.pagopa.common.web.exception.MerchantValidationException;
import it.gov.pagopa.merchant.constants.MerchantConstants;
import it.gov.pagopa.merchant.model.Merchant;
import it.gov.pagopa.merchant.service.pointofsales.PointOfSaleTransactionCheckService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MerchantValidatorTest {

  @Mock
  private PointOfSaleTransactionCheckService pointOfSaleTransactionCheckService;

  private MerchantValidator validator;

  private static final String MERCHANT_ID = "MERCHANT_ID";
  private static final String INITIATIVE_ID = "INITIATIVE_ID";

  @BeforeEach
  void setUp() {
    validator = new MerchantValidator(pointOfSaleTransactionCheckService);
  }

  @Test
  void validateMerchantWithdrawal_activationTooOld_throwsException() {
    Merchant merchant = new Merchant();
    merchant.setMerchantId(MERCHANT_ID);
    merchant.setActivationDate(LocalDateTime.now().minusDays(20));

    when(pointOfSaleTransactionCheckService.hasInProgressTransactions(MERCHANT_ID, INITIATIVE_ID)).thenReturn(false);
    when(pointOfSaleTransactionCheckService.hasProcessedTransactions(MERCHANT_ID, INITIATIVE_ID)).thenReturn(false);

    MerchantValidationException ex = assertThrows(MerchantValidationException.class,
        () -> validator.validateMerchantWithdrawal(merchant, INITIATIVE_ID));

    assert ex.getErrors().stream()
        .anyMatch(e -> e.getCode().equals(MerchantConstants.CODE_CONTRACT_WITHDRAWAL_TOO_LATE));
  }

  @Test
  void validateMerchantWithdrawal_inProgressTransactions_throwsException() {
    Merchant merchant = new Merchant();
    merchant.setMerchantId(MERCHANT_ID);
    merchant.setActivationDate(LocalDateTime.now().minusDays(5));

    when(pointOfSaleTransactionCheckService.hasInProgressTransactions(MERCHANT_ID, INITIATIVE_ID)).thenReturn(true);
    when(pointOfSaleTransactionCheckService.hasProcessedTransactions(MERCHANT_ID, INITIATIVE_ID)).thenReturn(false);

    MerchantValidationException ex = assertThrows(MerchantValidationException.class,
        () -> validator.validateMerchantWithdrawal(merchant, INITIATIVE_ID));

    assert ex.getErrors().stream()
        .anyMatch(e -> e.getCode().equals(MerchantConstants.CODE_TRANSACTIONS_IN_PROGRESS_PRESENT));
  }

  @Test
  void validateMerchantWithdrawal_processedTransactions_throwsException() {
    Merchant merchant = new Merchant();
    merchant.setMerchantId(MERCHANT_ID);
    merchant.setActivationDate(LocalDateTime.now().minusDays(5));

    when(pointOfSaleTransactionCheckService.hasInProgressTransactions(MERCHANT_ID, INITIATIVE_ID)).thenReturn(false);
    when(pointOfSaleTransactionCheckService.hasProcessedTransactions(MERCHANT_ID, INITIATIVE_ID)).thenReturn(true);

    MerchantValidationException ex = assertThrows(MerchantValidationException.class,
        () -> validator.validateMerchantWithdrawal(merchant, INITIATIVE_ID));

    assert ex.getErrors().stream()
        .anyMatch(e -> e.getCode().equals(MerchantConstants.CODE_TRANSACTIONS_PROCESSED_PRESENT));
  }

  @Test
  void validateMerchantWithdrawal_noErrors_passes() {
    Merchant merchant = new Merchant();
    merchant.setMerchantId(MERCHANT_ID);
    merchant.setActivationDate(LocalDateTime.now().minusDays(5));

    when(pointOfSaleTransactionCheckService.hasInProgressTransactions(MERCHANT_ID, INITIATIVE_ID)).thenReturn(false);
    when(pointOfSaleTransactionCheckService.hasProcessedTransactions(MERCHANT_ID, INITIATIVE_ID)).thenReturn(false);

    validator.validateMerchantWithdrawal(merchant, INITIATIVE_ID);
  }

  @Test
  void validateMerchantWithdrawal_activationDateNull_throwsException() {
    Merchant merchant = new Merchant();
    merchant.setMerchantId(MERCHANT_ID);
    merchant.setActivationDate(null);

    MerchantValidationException ex = assertThrows(MerchantValidationException.class,
        () -> validator.validateMerchantWithdrawal(merchant, INITIATIVE_ID));

    assert ex.getErrors().stream()
        .anyMatch(e -> e.getCode().equals(MerchantConstants.CODE_ACTIVATION_DATE_MISSING));
  }
}
