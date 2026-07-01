package it.gov.pagopa.merchant.service.pointofsales;

import it.gov.pagopa.merchant.constants.MerchantConstants;
import it.gov.pagopa.merchant.constants.PointOfSaleConstants;
import it.gov.pagopa.merchant.dto.MerchantDetailDTO;
import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleInitiativeDTO;
import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleInitiativeListDTO;
import it.gov.pagopa.merchant.exception.custom.MerchantNotFoundException;
import it.gov.pagopa.merchant.exception.custom.PointOfSaleNotFoundException;
import it.gov.pagopa.merchant.mapper.PointOfSaleInitiativeDTOMapper;
import it.gov.pagopa.merchant.model.Initiative;
import it.gov.pagopa.merchant.model.Merchant;
import it.gov.pagopa.merchant.model.PointOfSale;
import it.gov.pagopa.merchant.model.PointOfSalesInitiative;
import it.gov.pagopa.merchant.repository.MerchantRepository;
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

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PointOfSaleInitiativeFinderServiceImpl implements PointOfSaleInitiativeFinderService {
    private final MerchantRepository merchantRepository;
    private final PointOfSaleRepository pointOfSaleRepository;
    private final PointOfSalesInitiativeRepository pointOfSalesInitiativeRepository;
    private final MerchantService merchantService;

    private final PointOfSaleInitiativeDTOMapper pointOfSaleInitiativeDTOMapper;

    public PointOfSaleInitiativeFinderServiceImpl(PointOfSaleRepository pointOfSaleRepository,
                                                  PointOfSalesInitiativeRepository pointOfSalesInitiativeRepository,
                                                  MerchantService merchantService,
                                                  MerchantRepository merchantRepository,
                                                  PointOfSaleInitiativeDTOMapper pointOfSaleInitiativeDTOMapper) {
        this.pointOfSaleRepository = pointOfSaleRepository;
        this.pointOfSalesInitiativeRepository = pointOfSalesInitiativeRepository;
        this.merchantService = merchantService;
        this.merchantRepository = merchantRepository;
        this.pointOfSaleInitiativeDTOMapper = pointOfSaleInitiativeDTOMapper;
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
                    Collections.emptyList(), Utilities.getPageable(pageable), () -> 0L);
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

    @Override
    public PointOfSaleInitiativeListDTO getInitiativesByPointOfSaleId(String pointOfSaleId, String merchantId) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new MerchantNotFoundException(merchantId));

        Map<String, Initiative> initiativeMap = merchant.getInitiativeList().stream()
                .collect(Collectors.toMap(
                        Initiative::getInitiativeId,
                        Function.identity()
                ));


        List<PointOfSalesInitiative> posInitiatives =
                pointOfSalesInitiativeRepository.findByPointOfSaleId(pointOfSaleId);

        List<PointOfSaleInitiativeDTO> validInitiatives = new ArrayList<>();

        for (PointOfSalesInitiative posInitiative : posInitiatives) {
            String initiativeId = posInitiative.getInitiativeId();

            Initiative initiative = initiativeMap.get(initiativeId);

            if (initiative != null) {
                validInitiatives.add(pointOfSaleInitiativeDTOMapper.initiativeEntityToDto(initiative));
            } else {
                String sanitizedInitiativeId = Utilities.sanitizeString(initiativeId);
                String sanitizedPointOfSaleId = Utilities.sanitizeString(pointOfSaleId);
                String sanitizedMerchantId = Utilities.sanitizeString(merchantId);
                log.warn(
                        "[POS-INITIATIVES] Initiative [{}] linked to point of sale [{}] is not associated to merchant [{}]. Skipping.",
                        sanitizedInitiativeId, sanitizedPointOfSaleId, sanitizedMerchantId
                );
            }
        }

        return PointOfSaleInitiativeListDTO.builder()
                .initiatives(validInitiatives)
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
