package it.gov.pagopa.merchant.model;

import lombok.*;
import lombok.experimental.FieldNameConstants;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldNameConstants
public class Channel {
    private String type;
    private String contact;
}
