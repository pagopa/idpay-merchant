package it.gov.pagopa.merchant.repository;

import java.util.List;

public interface PointOfSalesInitiativeRepositoryExtended {

    List<String> findPointOfSaleIdsByInitiativeIdAndMerchantIdAndEnabledTrue(
            String initiativeId, String merchantId
    );
}
