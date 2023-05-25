package it.gov.pagopa.merchant.constants;

public class MerchantConstants {
    private MerchantConstants(){}
    public static final String CONTENT_TYPE = "text/csv";
    public static final String MERCHANT_FILE_PATH_TEMPLATE = "%s/%s/%s";

    public static final class Status {
        private Status(){}
        public static final String PROCESSED = "PROCESSED";
        public static final String ON_EVALUATION = "ON_EVALUATION";
        public static final String STORAGE_KO = "STORAGE_KO";
        public static final String DOWNLOAD_KO = "DOWNLOAD_KO";
        public static final String MERCHANT_SAVING_KO = "MERCHANT_SAVING_KO";
        public static final String VALIDATED = "VALIDATED";
        public static final String KO = "KO";

        public static final class KOkeyMessage {
            private KOkeyMessage(){}
            public static final String INVALID_FILE_EMPTY = "merchant.invalid.file.empty";
            public static final String INVALID_FILE_FORMAT = "merchant.invalid.file.format";
            public static final String INVALID_FILE_NAME = "merchant.invalid.file.name";
            public static final String MISSING_REQUIRED_FIELDS = "merchant.missing.required.fields";
            public static final String INVALID_FILE_CF_WRONG = "merchant.invalid.file.cf.wrong";
            public static final String INVALID_FILE_IBAN_WRONG = "merchant.invalid.file.iban.wrong";
            public static final String INVALID_FILE_EMAIL_WRONG = "merchant.invalid.file.email.wrong";

        }
    }

    public static final String NOT_FOUND = "NOT FOUND";
    public static final String INTERNAL_SERVER_ERROR = "INTERNAL SERVER ERROR";
    public static final String INITIATIVE_AND_MERCHANT_NOT_FOUND = "Initiative %s and merchant %s not found.";
    public static final String CSV_READING_ERROR = "Initiative %s - file %s: error during file reading - %s";
    public static final String STORAGE_ERROR = "Initiative %s - file %s: error during file storage";
    public static final String DOWNLOAD_ERROR = "Initiative %s - file %s: error during file download";
    public static final String MERCHANT_SAVING_ERROR = "Initiative %s - file %s: error during merchant saving";

}