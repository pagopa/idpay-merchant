package it.gov.pagopa.merchant.dto.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ChannelTypeEnum {

    WEB("WEB"),
    EMAIL("EMAIL"),
    MOBILE("MOBILE"),
    LANDING("LANDING");

    private final String value;

    ChannelTypeEnum(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static ChannelTypeEnum fromValue(String text) {
        for (ChannelTypeEnum b : ChannelTypeEnum.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }
}
