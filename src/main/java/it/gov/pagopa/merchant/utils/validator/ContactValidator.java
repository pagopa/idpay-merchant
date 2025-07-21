package it.gov.pagopa.merchant.utils.validator;

import io.micrometer.common.util.StringUtils;
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

        if(StringUtils.isBlank(contact)){
            return true;
        }

        boolean valid;
        String message;

        switch (dto.getType()) {
            case WEB, LANDING:
                valid = contact.matches(REGEX_WEB);
                message = "Invalid WEB/LANDING format: "+contact;
                break;
            case EMAIL:
                valid = contact.matches(REGEX_EMAIL);
                message = "Invalid EMAIL format: "+contact;
                break;
            case MOBILE:
                valid = contact.matches(REGEX_MOBILE);
                message = "Invalid MOBILE format: "+contact;
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