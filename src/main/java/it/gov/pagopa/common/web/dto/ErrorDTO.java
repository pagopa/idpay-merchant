package it.gov.pagopa.common.web.dto;

import it.gov.pagopa.common.web.exception.ServiceExceptionPayload;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.SuperBuilder;


@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode
@SuperBuilder
public class ErrorDTO implements ServiceExceptionPayload {

    @NotBlank
    private String code;
    @NotBlank
    private String message;
}
