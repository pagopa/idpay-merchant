package it.gov.pagopa.merchant.service.pointofsales;

import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleReferentPatchDTO;
import it.gov.pagopa.merchant.model.PointOfSale;

public interface UpdatePointOfSaleReferentService {

    PointOfSale updateReferent(String merchantId, String pointOfSaleId,
        PointOfSaleReferentPatchDTO referentPatchDTO);
}
