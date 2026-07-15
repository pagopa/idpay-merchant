package it.gov.pagopa.merchant.repository;

import it.gov.pagopa.merchant.model.PointOfSalesInitiative;
import java.util.List;

public interface PointOfSalesInitiativeRepositoryExtended {

    List<String> findPointOfSaleIdsByInitiativeIdAndMerchantIdAndEnabledTrue(
            String initiativeId, String merchantId
    );

    List<String> findPointOfSaleIdsByMerchantIdAndEnabledTrue(String merchantId);

    List<PointOfSalesInitiative> findInitiativesByPointOfSaleIdAndMerchantIdAndEnabledTrue(
            String pointOfSaleId, String merchantId
    );
}
