package it.gov.pagopa.merchant.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
public class Initiative {
    private String initiativeId;
    private String initiativeName;
    private String organizationId;
    private String organizationName;
    private String serviceId;
    private String status;
    private Instant startDate;
    private Instant endDate;
    private String merchantStatus;
    private Instant creationDate;
    private Instant updateDate;
    private boolean enabled;

}
