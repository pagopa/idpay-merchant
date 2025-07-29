package it.gov.pagopa.merchant.exception.custom;

import it.gov.pagopa.common.web.exception.ServiceException;
import it.gov.pagopa.merchant.constants.PointOfSaleConstants;


public class PointOfSaleNotFoundException extends ServiceException {

    public PointOfSaleNotFoundException(String message) {
        super(PointOfSaleConstants.CODE_NOT_FOUND, message, null, false, null);
    }

}
