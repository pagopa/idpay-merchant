package it.gov.pagopa.common.web.dto;

import it.gov.pagopa.common.web.exception.ServiceExceptionPayload;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode
public class ErrorDTO implements ServiceExceptionPayload {

    @NotBlank
    private String code;
    @NotBlank
    private String message;
}
