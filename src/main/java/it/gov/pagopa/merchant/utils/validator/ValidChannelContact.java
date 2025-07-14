package it.gov.pagopa.merchant.utils.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ContactValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidChannelContact {
    String message() default "Contatto non valido";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
