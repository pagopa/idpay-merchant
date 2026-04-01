package it.gov.pagopa.merchant.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitiativeDTO {
    @NotBlank
    private String initiativeId;
    @NotBlank
    private String initiativeName;
    private String organizationId;
    private String organizationName;
    private String serviceId;
    private String status;
    private Instant startDate;
    private Instant endDate;
    private String merchantStatus;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Instant creationDate;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Instant updateDate;
    private boolean enabled;
}
