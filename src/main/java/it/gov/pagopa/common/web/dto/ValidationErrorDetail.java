package it.gov.pagopa.common.web.dto;

import lombok.*;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ValidationErrorDetail {

    private int index;
    private String field;
    private Object value;
    private String code;
    private String message;

}
