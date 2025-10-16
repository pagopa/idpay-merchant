package it.gov.pagopa.merchant.constants;

public class MerchantConstants {

  private MerchantConstants() {
  }

  public static final String CONTENT_TYPE = "text/csv";
  public static final String MERCHANT_FILE_PATH_TEMPLATE = "%s/%s/%s";

  public static final class Status {

    private Status() {
    }

    public static final String PROCESSED = "PROCESSED";
    public static final String ON_EVALUATION = "ON_EVALUATION";
    public static final String STORAGE_KO = "STORAGE_KO";
    public static final String DOWNLOAD_KO = "DOWNLOAD_KO";
    public static final String MERCHANT_SAVING_KO = "MERCHANT_SAVING_KO";
    public static final String VALIDATED = "VALIDATED";
    public static final String KO = "KO";
    public static final String INITIATIVE_NOT_FOUND = "INITIATIVE_NOT_FOUND";

    public static final class KOkeyMessage {

      private KOkeyMessage() {
      }

      public static final String INVALID_FILE_EMPTY = "merchant.invalid.file.empty";
      public static final String INVALID_FILE_FORMAT = "merchant.invalid.file.format";
      public static final String INVALID_FILE_NAME = "merchant.invalid.file.name";
      public static final String MISSING_REQUIRED_FIELDS = "merchant.missing.required.fields";
      public static final String INVALID_FILE_ACQUIRER_WRONG = "merchant.invalid.file.acquirer.wrong";
      public static final String INVALID_FILE_CF_WRONG = "merchant.invalid.file.cf.wrong";
      public static final String INVALID_FILE_IBAN_WRONG = "merchant.invalid.file.iban.wrong";
      public static final String INVALID_FILE_EMAIL_WRONG = "merchant.invalid.file.email.wrong";

    }
  }

  public static final class ExceptionCode {

    private ExceptionCode() {
    }

    public static final String MERCHANT_NOT_FOUND = "MERCHANT_NOT_FOUND";
    public static final String MERCHANT_NOT_ONBOARDED = "MERCHANT_NOT_ONBOARDED";
    public static final String INVALID_REQUEST = "MERCHANT_INVALID_REQUEST";
    public static final String TOO_MANY_REQUESTS = "MERCHANT_TOO_MANY_REQUESTS";
    public static final String GENERIC_ERROR = "MERCHANT_GENERIC_ERROR";
    public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
  }

  public static final class ExceptionMessage {

    private ExceptionMessage() {
    }

    public static final String INITIATIVE_AND_MERCHANT_NOT_FOUND = "The current merchant is not onboarded on initiative [%s]";
    public static final String MERCHANT_NOT_FOUND_MESSAGE = "Cannot find current merchant %s";
    public static final String CSV_READING_ERROR = "An error occurred during file reading";
    public static final String STORAGE_ERROR = "An error occurred during file storage";
    public static final String DOWNLOAD_ERROR = "An error occurred during file download";
    public static final String MERCHANT_SAVING_ERROR = "An error occurred during file saving";
    public static final String INITIATIVE_CONNECTOR_ERROR = "An error occurred in the microservice initiative";
    public static final String VALIDATION_ERROR = "Some fields are invalid. See details for more information.";
  }

  public static final String CODE_MERCHANT_WITHDRAWAL_ERROR = "MERCHANT_WITHDRAWAL_ERROR";
  public static final String CODE_CONTRACT_WITHDRAWAL_TOO_LATE = "CONTRACT_WITHDRAWAL_TOO_LATE";
  public static final String CODE_TRANSACTIONS_IN_PROGRESS_PRESENT = "TRANSACTIONS_IN_PROGRESS_PRESENT";
  public static final String CODE_TRANSACTIONS_PROCESSED_PRESENT = "TRANSACTIONS_PROCESSED_PRESENT";
  public static final String CODE_ACTIVATION_DATE_MISSING = "ACTIVATION_DATE_MISSING";

  public static final String MSG_CONTRACT_WITHDRAWAL_TOO_LATE =
      "Withdrawal is not permitted: more than 15 days have passed since the activation date.";
  public static final String MSG_TRANSACTIONS_IN_PROGRESS_PRESENT =
      "Unable to withdraw merchant: there are transactions in progress associated with the points of sale.";
  public static final String MSG_TRANSACTIONS_PROCESSED_PRESENT =
      "Unable to withdraw merchant: there are transactions processed associated with the points of sale.";
  public static final String MSG_ACTIVATION_DATE_MISSING = "Activation date is required";

  public static final String INITIATIVE_CLOSED = "CLOSED";
  public static final String INITIATIVE_PUBLISHED = "PUBLISHED";

  public static final String OPERATION_TYPE_DELETE_INITIATIVE = "DELETE_INITIATIVE";
  public static final String OPERATION_TYPE_CREATE_MERCHANT_STATISTICS = "CREATE_MERCHANT_STATISTICS";
}