package it.gov.pagopa.merchant.service.pointofsales;

import it.gov.pagopa.merchant.constants.MerchantConstants;
import it.gov.pagopa.merchant.constants.PointOfSaleConstants;
import it.gov.pagopa.merchant.dto.MerchantDetailDTO;
import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleInitiativeDTO;
import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleInitiativeListDTO;
import it.gov.pagopa.merchant.exception.custom.MerchantNotFoundException;
import it.gov.pagopa.merchant.exception.custom.PointOfSaleNotFoundException;
import it.gov.pagopa.merchant.model.PointOfSale;
import it.gov.pagopa.merchant.model.PointOfSalesInitiative;
import it.gov.pagopa.merchant.repository.PointOfSaleRepository;
import it.gov.pagopa.merchant.repository.PointOfSalesInitiativeRepository;
import it.gov.pagopa.merchant.service.MerchantService;
import it.gov.pagopa.merchant.utils.Utilities;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class GetPointOfSaleWithInitiativeServiceImpl implements GetPointOfSaleWithInitiativeService{

    private final PointOfSaleRepository pointOfSaleRepository;
    private final PointOfSalesInitiativeRepository pointOfSalesInitiativeRepository;
    private final MerchantService merchantService;

    public GetPointOfSaleWithInitiativeServiceImpl(PointOfSaleRepository pointOfSaleRepository,
                                                   PointOfSalesInitiativeRepository pointOfSalesInitiativeRepository,
                                                   MerchantService merchantService) {
        this.pointOfSaleRepository = pointOfSaleRepository;
        this.pointOfSalesInitiativeRepository = pointOfSalesInitiativeRepository;
        this.merchantService = merchantService;
    }

    @Override
    public PointOfSale getPointOfSaleByIdAndMerchantIdAndInitiativeId(
            String initiativeId, String pointOfSaleId, String merchantId) {
        verifyMerchantExists(merchantId);

        pointOfSalesInitiativeRepository.findByPointOfSaleIdAndInitiativeIdAndMerchantIdAndEnabledTrue(
                        pointOfSaleId, initiativeId, merchantId)
                .orElseThrow(() -> new PointOfSaleNotFoundException(
                        String.format(PointOfSaleConstants.MSG_NOT_FOUND, pointOfSaleId)
                ));

        return findPointOfSaleByIdAndMerchantId(pointOfSaleId, merchantId);
    }

    @Override
    public Page<PointOfSale> getPointOfSalesListByInitiative(
            String initiativeId,
            String merchantId,
            String type,
            String city,
            String address,
            String contactName,
            Pageable pageable) {

        verifyMerchantExists(merchantId);

        List<String> pointOfSaleIds = pointOfSalesInitiativeRepository
                .findPointOfSaleIdsByInitiativeIdAndMerchantIdAndEnabledTrue(
                        initiativeId, merchantId);

        if (pointOfSaleIds.isEmpty()) {
            return PageableExecutionUtils.getPage(
                    Collections.<PointOfSale>emptyList(), Utilities.getPageable(pageable), () -> 0L);
        }

        Criteria criteria = pointOfSaleRepository.getCriteria(merchantId, pointOfSaleIds, type, city,
                address, contactName);
        return getPointOfSalesPage(criteria, pageable);
    }

    @Override
    public PointOfSaleInitiativeListDTO getInitiativesByPointOfSaleIdAndMerchantId(
            String pointOfSaleId, String merchantId) {
        verifyMerchantExists(merchantId);
        List<PointOfSaleInitiativeDTO> initiatives = pointOfSalesInitiativeRepository
                .findInitiativesByPointOfSaleIdAndMerchantIdAndEnabledTrue(pointOfSaleId, merchantId)
                .stream()
                .map(this::toPointOfSaleInitiativeDTO)
                .toList();

        return PointOfSaleInitiativeListDTO.builder()
                .initiatives(initiatives)
                .build();
    }

    protected Page<PointOfSale> getPointOfSalesPage(Criteria criteria, Pageable pageable) {
        List<PointOfSale> matched = pointOfSaleRepository.findByFilter(criteria, pageable);
        long total = pointOfSaleRepository.getCount(criteria);

        return PageableExecutionUtils.getPage(matched, Utilities.getPageable(pageable), () -> total);
    }

    protected void verifyMerchantExists(String merchantId) {
        MerchantDetailDTO merchantDetail = merchantService.getMerchantDetail(merchantId);
        if (merchantDetail == null) {
            throw new MerchantNotFoundException(
                    String.format(MerchantConstants.ExceptionMessage.MERCHANT_NOT_FOUND_MESSAGE, merchantId));
        }
    }

    protected PointOfSale findPointOfSaleByIdAndMerchantId(String pointOfSaleId, String merchantId) {
        return pointOfSaleRepository.findByIdAndMerchantId(pointOfSaleId, merchantId)
                .orElseThrow(() -> new PointOfSaleNotFoundException(
                        String.format(PointOfSaleConstants.MSG_NOT_FOUND, pointOfSaleId)
                ));
    }

    private PointOfSaleInitiativeDTO toPointOfSaleInitiativeDTO(
            PointOfSalesInitiative pointOfSalesInitiative) {
        return PointOfSaleInitiativeDTO.builder()
                .initiativeId(pointOfSalesInitiative.getInitiativeId())
                .createdAt(pointOfSalesInitiative.getCreatedAt())
                .updatedAt(pointOfSalesInitiative.getUpdatedAt())
                .build();
    }
}
