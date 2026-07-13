package it.gov.pagopa.merchant.service.pointofsales;

import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleDTO;
import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleExclusionResultDTO;
import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleOnboardingResultDTO;

import java.util.List;

public interface PointOfSaleWriter {

    void savePointOfSales(String merchantId, String initiativeId, List<PointOfSaleDTO>  pointOfSaleList);

    PointOfSaleOnboardingResultDTO onboardingPointOfSales(
            String merchantId,
            String initiativeId,
            List<String> pointOfSaleIds);

    PointOfSaleExclusionResultDTO excludePointsOfSales(
            String merchantId,
            String initiativeId,
            List<String> pointOfSaleIds);

}
