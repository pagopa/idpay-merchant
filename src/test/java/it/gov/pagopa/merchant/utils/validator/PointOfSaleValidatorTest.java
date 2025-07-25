package it.gov.pagopa.merchant.utils.validator;

import it.gov.pagopa.common.web.exception.ClientExceptionWithBody;
import it.gov.pagopa.common.web.exception.ValidationException;
import it.gov.pagopa.merchant.dto.enums.PointOfSaleTypeEnum;
import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleDTO;
import it.gov.pagopa.merchant.test.fakers.PointOfSaleDTOFaker;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.metadata.ConstraintDescriptor;
import org.hibernate.validator.constraints.URL;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.lang.annotation.Annotation;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

@ContextConfiguration(classes = {PointOfSaleValidator.class})
@ExtendWith(SpringExtension.class)
class PointOfSaleValidatorTest {

    @Autowired
    private PointOfSaleValidator pointOfSaleValidator;

    @MockitoBean
    private Validator validator;

    @Test
    void testValidateViolationsPointOfSales() {
        Validator validator = mock(Validator.class);
        when(validator.validate(Mockito.<PointOfSaleDTO>any(), isA(Class[].class))).thenReturn(new HashSet<>());
        PointOfSaleValidator pointOfSaleValidator = new PointOfSaleValidator(validator);

        ArrayList<PointOfSaleDTO> pointOfSaleDTOS = new ArrayList<>();
        PointOfSaleDTO buildResult = PointOfSaleDTOFaker.mockInstance();
        buildResult.setType(PointOfSaleTypeEnum.PHYSICAL);
        buildResult.setContactEmail("");

        pointOfSaleDTOS.add(buildResult);

        ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(violation.getPropertyPath()).thenReturn(path);
        Set<ConstraintViolation<Object>> violationSet = Set.of(violation);

        ConstraintDescriptor<Annotation> constraintDescriptor = mock(ConstraintDescriptor.class);
        when(violation.getConstraintDescriptor()).thenReturn((ConstraintDescriptor) constraintDescriptor);
        Annotation annotation = new Annotation() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return NotBlank.class;
            }
        };
        when(constraintDescriptor.getAnnotation()).thenReturn(annotation);

        when(validator.validate(any(),any())).thenReturn(violationSet);
        // Act and Assert
        assertThrows(ValidationException.class, () -> pointOfSaleValidator.validateViolationsPointOfSales(pointOfSaleDTOS));
    }


    @Test
    void testValidateViolationsPointOfSales1() {
        Validator validator = mock(Validator.class);
        when(validator.validate(Mockito.<PointOfSaleDTO>any(), isA(Class[].class))).thenReturn(new HashSet<>());
        PointOfSaleValidator pointOfSaleValidator = new PointOfSaleValidator(validator);

        ArrayList<PointOfSaleDTO> pointOfSaleDTOS = new ArrayList<>();
        PointOfSaleDTO buildResult = PointOfSaleDTOFaker.mockInstance();
        buildResult.setType(PointOfSaleTypeEnum.PHYSICAL);
        buildResult.setContactEmail("");

        pointOfSaleDTOS.add(buildResult);

        buildResult.setType(PointOfSaleTypeEnum.ONLINE);
        buildResult.setWebsite("");
        pointOfSaleDTOS.add(buildResult);

        ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(violation.getPropertyPath()).thenReturn(path);
        Set<ConstraintViolation<Object>> violationSet = Set.of(violation);

        ConstraintDescriptor<Annotation> constraintDescriptor = mock(ConstraintDescriptor.class);
        when(violation.getConstraintDescriptor()).thenReturn((ConstraintDescriptor) constraintDescriptor);
        Annotation annotation = new Annotation() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return Pattern.class;
            }
        };
        when(constraintDescriptor.getAnnotation()).thenReturn(annotation);

        when(validator.validate(any(),any())).thenReturn(violationSet);
        // Act and Assert
        assertThrows(ValidationException.class, () -> pointOfSaleValidator.validateViolationsPointOfSales(pointOfSaleDTOS));
    }

    @Test
    void testValidateViolationsPointOfSales2() {
        Validator validator = mock(Validator.class);
        when(validator.validate(Mockito.<PointOfSaleDTO>any(), isA(Class[].class))).thenReturn(new HashSet<>());
        PointOfSaleValidator pointOfSaleValidator = new PointOfSaleValidator(validator);

        ArrayList<PointOfSaleDTO> pointOfSaleDTOS = new ArrayList<>();
        PointOfSaleDTO buildResult = PointOfSaleDTOFaker.mockInstance();
        buildResult.setType(PointOfSaleTypeEnum.PHYSICAL);
        buildResult.setContactEmail("");

        pointOfSaleDTOS.add(buildResult);

        buildResult.setType(PointOfSaleTypeEnum.ONLINE);
        buildResult.setWebsite("");
        pointOfSaleDTOS.add(buildResult);

        ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(violation.getPropertyPath()).thenReturn(path);
        Set<ConstraintViolation<Object>> violationSet = Set.of(violation);

        ConstraintDescriptor<Annotation> constraintDescriptor = mock(ConstraintDescriptor.class);
        when(violation.getConstraintDescriptor()).thenReturn((ConstraintDescriptor) constraintDescriptor);
        Annotation annotation = new Annotation() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return URL.class;
            }
        };
        when(constraintDescriptor.getAnnotation()).thenReturn(annotation);

        when(validator.validate(any(),any())).thenReturn(violationSet);
        // Act and Assert
        assertThrows(ValidationException.class, () -> pointOfSaleValidator.validateViolationsPointOfSales(pointOfSaleDTOS));
    }



    @Test
    void testValidateViolationsPointOfSales4() {
        Validator validator = mock(Validator.class);
        when(validator.validate(Mockito.<PointOfSaleDTO>any(), isA(Class[].class))).thenReturn(new HashSet<>());
        PointOfSaleValidator pointOfSaleValidator = new PointOfSaleValidator(validator);

        ArrayList<PointOfSaleDTO> pointOfSaleDTOS = new ArrayList<>();
        PointOfSaleDTO buildResult = PointOfSaleDTOFaker.mockInstance();
        buildResult.setType(PointOfSaleTypeEnum.PHYSICAL);
        buildResult.setContactEmail("");

        pointOfSaleDTOS.add(buildResult);

        buildResult.setType(PointOfSaleTypeEnum.ONLINE);
        buildResult.setWebsite("");
        pointOfSaleDTOS.add(buildResult);

        ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(violation.getPropertyPath()).thenReturn(path);
        Set<ConstraintViolation<Object>> violationSet = Set.of(violation);

        ConstraintDescriptor<Annotation> constraintDescriptor = mock(ConstraintDescriptor.class);
        when(violation.getConstraintDescriptor()).thenReturn((ConstraintDescriptor) constraintDescriptor);
        Annotation annotation = new Annotation() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return Email.class;
            }
        };
        when(constraintDescriptor.getAnnotation()).thenReturn(annotation);

        when(validator.validate(any(),any())).thenReturn(violationSet);
        // Act and Assert
        assertThrows(ValidationException.class, () -> pointOfSaleValidator.validateViolationsPointOfSales(pointOfSaleDTOS));
    }

    @Test
    void validatePointOfSales() {
        ClientExceptionWithBody exception = assertThrows(ClientExceptionWithBody.class,
                () -> pointOfSaleValidator.validatePointOfSales(null));

        assertEquals("Point of sales list cannot be empty.", exception.getMessage());
    }

    @Test
    void validateViolationsPointOfSalesWhenPhysicalTypeShouldUsePhysicalGroup() {
        PointOfSaleDTO dto = PointOfSaleDTOFaker.mockInstance();
        dto.setType(PointOfSaleTypeEnum.PHYSICAL);

        List<PointOfSaleDTO> list = List.of(dto);

        when(validator.validate(dto, PhysicalGroup.class)).thenReturn(Collections.emptySet());

        assertDoesNotThrow(() -> pointOfSaleValidator.validateViolationsPointOfSales(list));

        verify(validator).validate(dto, PhysicalGroup.class);
    }

    @Test
    void validateViolationsPointOfSalesWhenOnlineTypeShouldUseOnlineGroup() {
        PointOfSaleDTO dto = PointOfSaleDTOFaker.mockInstance();
        dto.setType(PointOfSaleTypeEnum.ONLINE);

        List<PointOfSaleDTO> list = List.of(dto);

        when(validator.validate(dto, OnlineGroup.class)).thenReturn(Collections.emptySet());

        assertDoesNotThrow(() -> pointOfSaleValidator.validateViolationsPointOfSales(list));

        verify(validator).validate(dto, OnlineGroup.class);
    }


    @Test
    void validateEmailAndWebsiteInvalidEmailShouldAddError() {
        PointOfSaleDTO dto = PointOfSaleDTOFaker.mockInstance();
        dto.setContactEmail("invalidEmail");
        dto.setWebsite("notAnEmail.com");

        List<PointOfSaleDTO> list = List.of(dto);

        when(validator.validate(dto)).thenReturn(Collections.emptySet());

        ValidationException exception = Assertions.assertThrows(ValidationException.class, () -> {
            pointOfSaleValidator.validateViolationsPointOfSales(list);
        });

        Assertions.assertFalse(exception.getMessage().contains("contactEmail must be a valid EMAIL"));
    }



}