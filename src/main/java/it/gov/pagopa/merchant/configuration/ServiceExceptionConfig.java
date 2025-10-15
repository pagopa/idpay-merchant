package it.gov.pagopa.merchant.configuration;

import it.gov.pagopa.common.web.exception.ServiceException;
import it.gov.pagopa.merchant.exception.custom.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class ServiceExceptionConfig {

    @Bean
    public Map<Class<? extends ServiceException>, HttpStatus> serviceExceptionMapper() {
        Map<Class<? extends ServiceException>, HttpStatus> exceptionMap = new HashMap<>();

        // NotFound
        exceptionMap.put(MerchantNotFoundException.class, HttpStatus.NOT_FOUND);

        // InternalServerError
        exceptionMap.put(FileOperationException.class, HttpStatus.INTERNAL_SERVER_ERROR);
        exceptionMap.put(InitiativeInvocationException.class, HttpStatus.INTERNAL_SERVER_ERROR);
        exceptionMap.put(PaymentInvocationException.class, HttpStatus.INTERNAL_SERVER_ERROR);

        exceptionMap.put(PointOfSaleDuplicateException.class, HttpStatus.BAD_REQUEST);

        // NotFound
        exceptionMap.put(PointOfSaleNotFoundException.class, HttpStatus.NOT_FOUND);

        return exceptionMap;
    }
}
