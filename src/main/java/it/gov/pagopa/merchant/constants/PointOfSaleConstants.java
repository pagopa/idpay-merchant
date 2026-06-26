package it.gov.pagopa.merchant.constants;

public final class PointOfSaleConstants {

    private PointOfSaleConstants(){}

    // ===GENERIC CODES===
    public static final String CODE_GENERIC_SAVE_ERROR = "POINT_OF_SALE_GENERIC_SAVE_ERROR";
    public static final String CODE_FIELD_REQUIRED = "POINT_OF_SALE_FIELD_REQUIRED";
    public static final String CODE_INVALID_FORMAT = "POINT_OF_SALE_INVALID_FORMAT";
    public static final String CODE_INVALID_URL = "POINT_OF_SALE_INVALID_URL";
    public static final String CODE_INVALID_VALUE = "POINT_OF_SALE_INVALID_VALUE";
    public static final String CODE_BAD_REQUEST = "POINT_OF_SALE_BAD_REQUEST";
    public static final String CODE_NOT_FOUND = "POINT_OF_SALE_NOT_FOUND";
    public static final String CODE_ALREADY_REGISTERED = "POINT_OF_SALE_ALREADY_REGISTERED";
    public static final String CODE_DATA_INCONSISTENCY = "CODE_DATA_INCONSISTENCY";

    public static final String CODE_EMAIL_ALREADY_REGISTERED = "EMAIL_ALREADY_REGISTERED";
    public static final String CODE_POS_ALREADY_REGISTERED_OTHER_INITIATIVE = "POS_ALREADY_REGISTERED_OTHER_INITIATIVE";
    public static final String CODE_ONLINE_POS_ALREADY_REGISTERED = "ONLINE_POS_ALREADY_REGISTERED";
    public static final String CODE_PHYSICAL_POS_ALREADY_REGISTERED = "PHYSICAL_POS_ALREADY_REGISTERED";
    // ===GENERIC MESSAGES===


    // ===POINT OF SALE===
    public static final String MSG_GENERIC_SAVE_ERROR = "Unexpected error occurred while saving point of sales";
    public static final String MSG_LIST_NOT_EMPTY = "Point of sales list cannot be empty.";
    public static final String MSG_POINT_OF_SALE_SIZE_EXCEEDED = "A maximum of %s point of sale entries is allowed";
    public static final String MSG_NOT_FOUND = "Point of sale with id %s not found.";
    public static final String MSG_ALREADY_REGISTERED = "Point of sale already registered for _id or unique index constraint";
    public static final String POINT_OF_SALE_NOT_ALLOWED = "POINT_OF_SALE_NOT_ALLOWED";

    // ===MERCHANT===
    public static final String MERCHANT_NOT_ALLOWED = "MERCHANT_NOT_ALLOWED";

  // ===EMAIL===
    public static final String CODE_INVALID_EMAIL = CODE_INVALID_FORMAT;
    public static final String MSG_INVALID_EMAIL = "Email must be a valid email address.";

    // ===WEBSITE===
    public static final String CODE_INVALID_WEBSITE = CODE_INVALID_URL;
    public static final String MSG_INVALID_WEBSITE = "Website must be a valid HTTPS URL.";

    // ===MOBILE===
    public static final String CODE_INVALID_MOBILE = CODE_INVALID_FORMAT;
    public static final String MSG_INVALID_MOBILE = "Mobile number must be a valid (7-15 digits).";

}