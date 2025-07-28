package it.gov.pagopa.merchant.exception.custom;

import it.gov.pagopa.common.web.exception.ServiceException;
import it.gov.pagopa.merchant.constants.PointOfSaleConstants;


public class PointOfSaleDuplicateException extends ServiceException {

    public PointOfSaleDuplicateException(String message) {
        super(PointOfSaleConstants.CODE_ALREADY_REGISTERED, message, null, false, null);
    }

}
