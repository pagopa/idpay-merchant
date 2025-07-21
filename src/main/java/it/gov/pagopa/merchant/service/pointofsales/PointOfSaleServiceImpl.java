package it.gov.pagopa.merchant.service.pointofsales;

import it.gov.pagopa.merchant.constants.MerchantConstants;
import it.gov.pagopa.merchant.dto.MerchantDetailDTO;
import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleDTO;
import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleListDTO;
import it.gov.pagopa.merchant.exception.custom.MerchantNotFoundException;
import it.gov.pagopa.merchant.mapper.PointOfSaleDTOMapper;
import it.gov.pagopa.merchant.model.PointOfSale;
import it.gov.pagopa.merchant.repository.PointOfSaleRepository;
import it.gov.pagopa.merchant.service.MerchantService;
import it.gov.pagopa.merchant.utils.Utilities;
import it.gov.pagopa.merchant.utils.validator.PointOfSaleValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class PointOfSaleServiceImpl implements PointOfSaleService {

    private final MerchantService merchantService;
    private final PointOfSaleRepository pointOfSaleRepository;
    private final PointOfSaleDTOMapper pointOfSaleDTOMapper;
    private final PointOfSaleValidator pointOfSaleValidator;

    public PointOfSaleServiceImpl(
            MerchantService merchantService,
            PointOfSaleRepository pointOfSaleRepository,
            PointOfSaleDTOMapper pointOfSaleDTOMapper,
            PointOfSaleValidator pointOfSaleValidator) {
        this.merchantService = merchantService;
        this.pointOfSaleRepository = pointOfSaleRepository;
        this.pointOfSaleDTOMapper = pointOfSaleDTOMapper;
        this.pointOfSaleValidator = pointOfSaleValidator;
    }

    @Override
    public void savePointOfSales(String merchantId, List<PointOfSaleDTO> pointOfSaleDTOList){
        checkMerchantExist(merchantId);
        pointOfSaleValidator.validateViolationsPointOfSales(pointOfSaleDTOList);

        List<PointOfSale> pointOfSales = pointOfSaleDTOList.stream()
                .map(pointOfSaleDTO -> pointOfSaleDTOMapper.pointOfSaleDTOtoPointOfSaleEntity(pointOfSaleDTO,merchantId))
                .toList();
        pointOfSaleRepository.saveAll(pointOfSales);
    }

    @Override
    public PointOfSaleListDTO getPointOfSalesList(String merchantId, String type, String city, String address, String contactName, Pageable pageable) {
        checkMerchantExist(merchantId);

        Criteria criteria = pointOfSaleRepository.getCriteria(merchantId, type, city, address, contactName);

        List<PointOfSale> entities = pointOfSaleRepository.findByFilter(criteria, pageable);
        long count = pointOfSaleRepository.getCount(criteria);

        final Page<PointOfSale> entitiesPage = PageableExecutionUtils.getPage(entities,
                Utilities.getPageable(pageable), () -> count);

        Page<PointOfSaleDTO> result = entitiesPage.map(pointOfSaleDTOMapper::pointOfSaleEntityToPointOfSaleDTO);

        return PointOfSaleListDTO.builder()
                .content(result.getContent())
                .pageNo(result.getNumber())
                .pageSize(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .build();
    }

    private void checkMerchantExist(String merchantId){
        MerchantDetailDTO merchantDetail = merchantService.getMerchantDetail(merchantId);
        if(merchantDetail == null){
            throw new MerchantNotFoundException(
                    MerchantConstants.ExceptionCode.MERCHANT_NOT_ONBOARDED,
                    String.format(MerchantConstants.ExceptionMessage.MERCHANT_NOT_FOUND_MESSAGE,merchantId));
        }
    }

}
