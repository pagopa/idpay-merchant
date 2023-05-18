package it.gov.pagopa.merchant.constants;

public class MerchantConstants {
    public static final String CONTENT_TYPE = "text/csv";

    public static final class Status {
        public static final String VALIDATED = "VALIDATED";
        public static final String ON_EVALUATION = "ON_EVALUATION";
        public static final String KO = "KO";
        public static final class KOkeyMessage{
            public static final String INVALID_FILE_FORMAT          = "merchant.invalid.file.format";
            public static final String INVALID_FILE_EMPTY           = "merchant.invalid.file.empty";
            public static final String MISSING_REQUIRED_FIELDS            = "merchant.missing.required.fields";
            public static final String INVALID_FILE_VAT_WRONG              = "group.groups.invalid.file.vat.wrong";
            public static final String INVALID_FILE_IBAN_WRONG              = "group.groups.invalid.file.iban.wrong";
        }
    }

    public static final class Exception {
        public static final String BASE_CODE = "it.gov.pagopa.merchant";
        public static final class NotFound {
            public static final String CODE = BASE_CODE + ".not.found";
            public static final String INITIATIVE_BY_INITIATIVE_ID_MESSAGE = "Initiative with initiativeId %s not found.";
        }

    }
}
