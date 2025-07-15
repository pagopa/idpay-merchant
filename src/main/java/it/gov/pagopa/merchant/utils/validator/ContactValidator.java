package it.gov.pagopa.merchant.utils.validator;

import it.gov.pagopa.merchant.dto.pointofsales.ChannelDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ContactValidator implements ConstraintValidator<ValidChannelContact, ChannelDTO> {

    private static final String REGEX_WEB = "^https://[-a-zA-Z0-9+&@#/%?=|!:,.;]*[-a-zA-Z0-9+&@#/%=|]$";
    private static final String REGEX_EMAIL = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    private static final String REGEX_MOBILE = "^\\+?\\d{7,15}$";

    @Override
    public boolean isValid(ChannelDTO dto, ConstraintValidatorContext context) {
        if (dto == null || dto.getContact() == null || dto.getType() == null) {
            return true;
        }

        String contact = dto.getContact();

        boolean valid;
        String message;
        switch (dto.getType()) {
            case WEB, LANDING:
                valid = contact.matches(REGEX_WEB);
                message = String.format("Formato %s non valido: %s",dto.getType(),contact);
                break;
            case EMAIL:
                valid = contact.matches(REGEX_EMAIL);
                message = String.format("Formato EMAIL non valido: %s",contact);
                break;
            case MOBILE:
                valid = contact.matches(REGEX_MOBILE);
                message = String.format("Formato MOBILE non valido: %s",contact);
                break;
            default:
                return true;
        }

        if (!valid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message)
                    .addPropertyNode("contact")
                    .addConstraintViolation();
        }
        return valid;
    }
}