package it.gov.pagopa.common.web.dto;

import lombok.*;

import java.io.Serializable;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ValidationErrorDetail implements Serializable {

    private int index;
    private String field;
    private Object value;
    private String code;
    private String message;

}
