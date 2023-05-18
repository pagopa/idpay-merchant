package it.gov.pagopa.merchant.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
public class Initiative {
    private String initiativeId;
    private String initiativeName;
    private String merchantStatus;
    private LocalDateTime creationDate;
    private LocalDateTime updateDate;
}
