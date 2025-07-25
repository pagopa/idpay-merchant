package it.gov.pagopa.merchant.service.pointofsales;

import io.micrometer.common.util.StringUtils;
import it.gov.pagopa.common.web.exception.ClientExceptionWithBody;
import it.gov.pagopa.merchant.constants.MerchantConstants;
import it.gov.pagopa.merchant.constants.PointOfSaleConstants;
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
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.http.HttpStatus;
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
        verifyMerchantExists(merchantId);
        pointOfSaleValidator.validateViolationsPointOfSales(pointOfSaleDTOList);

        List<PointOfSale> entities = pointOfSaleDTOList.stream()
                .map(pointOfSaleDTO -> pointOfSaleDTOMapper.pointOfSaleDTOtoPointOfSaleEntity(pointOfSaleDTO,merchantId))
                .map(this::handleInsertOrUpdate)
                .toList();

        pointOfSaleRepository.saveAll(entities);
    }

    @Override
    public PointOfSaleListDTO getPointOfSalesList(
            String merchantId,
            String type,
            String city,
            String address,
            String contactName,
            Pageable pageable) {

        verifyMerchantExists(merchantId);

        Criteria criteria = pointOfSaleRepository.getCriteria(merchantId, type, city, address, contactName);
        List<PointOfSale> matched = pointOfSaleRepository.findByFilter(criteria, pageable);
        long total = pointOfSaleRepository.getCount(criteria);

        final Page<PointOfSale> entitiesPage = PageableExecutionUtils.getPage(matched,
                Utilities.getPageable(pageable), () -> total);

        Page<PointOfSaleDTO> result = entitiesPage.map(pointOfSaleDTOMapper::pointOfSaleEntityToPointOfSaleDTO);

        return PointOfSaleListDTO.builder()
                .content(result.getContent())
                .pageNo(result.getNumber())
                .pageSize(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .build();
    }

    private PointOfSale handleInsertOrUpdate(PointOfSale pointOfSale){
        ObjectId id = pointOfSale.getId();
        String contactEmail = pointOfSale.getContactEmail();

        List<PointOfSale> sameEmailList = pointOfSaleRepository.findByContactEmail(contactEmail);

        if(!sameEmailList.isEmpty()){
            throw new ClientExceptionWithBody(
                    HttpStatus.BAD_REQUEST,
                    PointOfSaleConstants.CODE_ALREADY_REGISTERED,
                    String.format(PointOfSaleConstants.MSG_ALREADY_REGISTERED,contactEmail));
        }

        boolean isInsert = id != null && StringUtils.isNotEmpty(id.toString());
        if(isInsert){
            PointOfSale pointOfSaleExisting = getPointOfSaleById(id.toString());
            pointOfSale.setCreationDate(pointOfSaleExisting.getCreationDate());
        }

        return pointOfSale;
    }


    private void verifyMerchantExists(String merchantId){
        MerchantDetailDTO merchantDetail = merchantService.getMerchantDetail(merchantId);
        if(merchantDetail == null){
            throw new MerchantNotFoundException(
                    MerchantConstants.ExceptionCode.MERCHANT_NOT_ONBOARDED,
                    String.format(MerchantConstants.ExceptionMessage.MERCHANT_NOT_FOUND_MESSAGE,merchantId));
        }
    }

    private PointOfSale getPointOfSaleById(String pointOfSaleId){
        return pointOfSaleRepository.findById(pointOfSaleId)
                .orElseThrow(() -> new ClientExceptionWithBody(
                        HttpStatus.NOT_FOUND,
                        PointOfSaleConstants.CODE_NOT_FOUND,
                        String.format(PointOfSaleConstants.MSG_NOT_FOUND,pointOfSaleId)));
    }

}
