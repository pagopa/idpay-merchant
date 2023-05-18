package it.gov.pagopa.merchant.constants;

public class MerchantConstants {
    public static final class Exception {
        public static final String BASE_CODE = "it.gov.pagopa.merchant";
        public static final class NotFound {
            public static final String CODE = BASE_CODE + ".not.found";
            public static final String INITIATIVE_BY_INITIATIVE_ID_MESSAGE = "Initiative with initiativeId %s not found.";
        }

    }
}
