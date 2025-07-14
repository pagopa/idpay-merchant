package it.gov.pagopa.merchant.utils.validator;

import it.gov.pagopa.merchant.dto.enums.ChannelTypeEnum;
import it.gov.pagopa.merchant.dto.pointofsales.ChannelDTO;
import jakarta.validation.ClockProvider;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintViolationCreationContext;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.messageinterpolation.ExpressionLanguageFeatureLevel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@ContextConfiguration(classes = {ContactValidator.class})
@ExtendWith(SpringExtension.class)
class ContactValidatorDiffblueTest {
    @Autowired
    private ContactValidator contactValidator;


    @Test
    void testIsValidWithChannelDTOConstraintValidatorContext() {
        // Arrange
        ChannelDTO dto = new ChannelDTO(null, null);

        ClockProvider clockProvider = mock(ClockProvider.class);

        // Act and Assert
        assertTrue(contactValidator.isValid(dto,
                new ConstraintValidatorContextImpl(clockProvider, PathImpl.createRootPath(), null,
                        "Constraint Validator Payload", ExpressionLanguageFeatureLevel.DEFAULT,
                        ExpressionLanguageFeatureLevel.DEFAULT)));
    }


    @Test
    void testIsValidWithChannelDTOConstraintValidatorContext_whenNull_thenReturnTrue() {
        // Arrange
        ClockProvider clockProvider = mock(ClockProvider.class);

        // Act and Assert
        assertTrue(contactValidator.isValid(null,
                new ConstraintValidatorContextImpl(clockProvider, PathImpl.createRootPath(), null,
                        "Constraint Validator Payload", ExpressionLanguageFeatureLevel.DEFAULT,
                        ExpressionLanguageFeatureLevel.DEFAULT)));
    }


    @Test
    void testIsValidWithChannelDTOConstraintValidatorContext2() {
        // Arrange
        ChannelDTO dto = new ChannelDTO(ChannelTypeEnum.WEB, "https://UU");

        ClockProvider clockProvider = mock(ClockProvider.class);

        // Act and Assert
        assertTrue(contactValidator.isValid(dto,
                new ConstraintValidatorContextImpl(clockProvider, PathImpl.createRootPath(), null,
                        "Constraint Validator Payload", ExpressionLanguageFeatureLevel.DEFAULT,
                        ExpressionLanguageFeatureLevel.DEFAULT)));
    }


    @Test
    void testIsValidWithChannelDTOConstraintValidatorContext3() {
        // Arrange
        ChannelDTO dto = new ChannelDTO(ChannelTypeEnum.WEB, "Contact");

        PathImpl propertyPath = PathImpl.createRootPath();
        propertyPath.addPropertyNode("https://UU");
        ConstraintValidatorContextImpl context = new ConstraintValidatorContextImpl(mock(ClockProvider.class), propertyPath,
                null, "Constraint Validator Payload", ExpressionLanguageFeatureLevel.DEFAULT,
                ExpressionLanguageFeatureLevel.DEFAULT);

        // Act
        boolean actualIsValidResult = contactValidator.isValid(dto, context);

        // Assert
        List<ConstraintViolationCreationContext> constraintViolationCreationContexts = context
                .getConstraintViolationCreationContexts();
        assertEquals(1, constraintViolationCreationContexts.size());
        ConstraintViolationCreationContext getResult = constraintViolationCreationContexts.getFirst();
        assertEquals("Formato WEB non valido: Contact", getResult.getMessage());
        assertNull(getResult.getDynamicPayload());
        assertEquals(ExpressionLanguageFeatureLevel.DEFAULT, getResult.getExpressionLanguageFeatureLevel());
        assertFalse(actualIsValidResult);
        assertFalse(getResult.getPath().isRootPath());
        Map<String, Object> expressionVariables = getResult.getExpressionVariables();
        assertTrue(expressionVariables.isEmpty());
        assertTrue(getResult.isCustomViolation());
        assertSame(expressionVariables, getResult.getMessageParameters());
    }


    @Test
    void testIsValidWithChannelDTOConstraintValidatorContext4() {
        // Arrange
        ContactValidator contactValidator = new ContactValidator();
        ChannelDTO dto = new ChannelDTO(ChannelTypeEnum.EMAIL, "U@U");

        ClockProvider clockProvider = mock(ClockProvider.class);

        // Act and Assert
        assertTrue(contactValidator.isValid(dto,
                new ConstraintValidatorContextImpl(clockProvider, PathImpl.createRootPath(), null,
                        "Constraint Validator Payload", ExpressionLanguageFeatureLevel.DEFAULT,
                        ExpressionLanguageFeatureLevel.DEFAULT)));
    }


    @Test
    void testIsValidWithChannelDTOConstraintValidatorContext5() {
        // Arrange
        ContactValidator contactValidator = new ContactValidator();
        ChannelDTO dto = new ChannelDTO(ChannelTypeEnum.MOBILE, "999999999");

        ClockProvider clockProvider = mock(ClockProvider.class);

        // Act and Assert
        assertTrue(contactValidator.isValid(dto,
                new ConstraintValidatorContextImpl(clockProvider, PathImpl.createRootPath(), null,
                        "Constraint Validator Payload", ExpressionLanguageFeatureLevel.DEFAULT,
                        ExpressionLanguageFeatureLevel.DEFAULT)));
    }


    @Test
    void testIsValidWithChannelDTOConstraintValidatorContext_givenAliceLiddellExampleOrg() {
        // Arrange
        ChannelDTO dto = ChannelDTO.builder().contact("Contact").type(ChannelTypeEnum.WEB).build();
        dto.setContact("alice.liddell@example.org");
        dto.setType(ChannelTypeEnum.EMAIL);
        ClockProvider clockProvider = mock(ClockProvider.class);

        // Act and Assert
        assertTrue(contactValidator.isValid(dto,
                new ConstraintValidatorContextImpl(clockProvider, PathImpl.createRootPath(), null,
                        "Constraint Validator Payload", ExpressionLanguageFeatureLevel.DEFAULT,
                        ExpressionLanguageFeatureLevel.DEFAULT)));
    }
}
