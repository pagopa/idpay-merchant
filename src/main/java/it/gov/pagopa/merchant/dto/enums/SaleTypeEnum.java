package it.gov.pagopa.merchant.dto.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SaleTypeEnum {
    FISICO("FISICO"),
    ONLINE("ONLINE");

    private final String value;

    SaleTypeEnum(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static SaleTypeEnum fromValue(String text) {
        for (SaleTypeEnum b : SaleTypeEnum.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }
}
