package it.gov.pagopa.merchant.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MerchantUpdateDTO {
    private String status;

    private Integer errorRow;

    private String errorKey;

    private Instant elabTimeStamp;
}
