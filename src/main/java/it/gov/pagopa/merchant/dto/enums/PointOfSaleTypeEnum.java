package it.gov.pagopa.merchant.dto.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PointOfSaleTypeEnum {

    PHYSICAL("PHYSICAL"),
    ONLINE("ONLINE");

    private final String value;

    PointOfSaleTypeEnum(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static PointOfSaleTypeEnum fromValue(String text) {
        for (PointOfSaleTypeEnum b : PointOfSaleTypeEnum.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }
}
