package it.gov.pagopa.merchant.constants;

public class MerchantConstants {
    public static final String CONTENT_TYPE = "text/csv";

    public static final class Status {

        public static final String VALIDATED = "VALIDATED";
        public static final String ON_EVALUATION = "ON_EVALUATION";
        public static final String KO = "KO";
    }

    public static final String NOT_FOUND = "NOT FOUND";
    public static final String INITIATIVE_AND_MERCHANT_NOT_FOUND = "Initiative %s and merchant %s not found.";
    public static final String MERCHANT_BY_MERCHANT_ID_MESSAGE = "Merchant %s not found.";

}
