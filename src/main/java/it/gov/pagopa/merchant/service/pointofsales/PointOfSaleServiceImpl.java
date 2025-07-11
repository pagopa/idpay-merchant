package it.gov.pagopa.merchant.service.pointofsales;

import it.gov.pagopa.common.web.exception.ClientExceptionWithBody;
import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleDTO;
import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleListDTO;
import it.gov.pagopa.merchant.mapper.PointOfSaleDTOMapper;
import it.gov.pagopa.merchant.model.PointOfSale;
import it.gov.pagopa.merchant.repository.PointOfSaleRepository;
import it.gov.pagopa.merchant.utils.Utilities;
import it.gov.pagopa.merchant.utils.validator.ValidationApiEnabledGroup;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PointOfSaleServiceImpl implements PointOfSaleService {

    private final PointOfSaleRepository pointOfSaleRepository;
    private final PointOfSaleDTOMapper pointOfSaleDTOMapper;
    private final Validator validator;

    public PointOfSaleServiceImpl(
            PointOfSaleRepository pointOfSaleRepository,
            PointOfSaleDTOMapper pointOfSaleDTOMapper,
            Validator validator) {
        this.pointOfSaleRepository = pointOfSaleRepository;
        this.pointOfSaleDTOMapper = pointOfSaleDTOMapper;
        this.validator = validator;
    }

    @Override
    public void savePointOfSales(String merchantId, List<PointOfSaleDTO> pointOfSaleDTOList){
        checkViolations(pointOfSaleDTOList);
        List<PointOfSale> pointOfSales = pointOfSaleDTOList.stream()
                .map(pointOfSaleDTO -> pointOfSaleDTOMapper.pointOfSaleDTOtoPointOfSaleEntity(pointOfSaleDTO,merchantId))
                .toList();
        pointOfSaleRepository.saveAll(pointOfSales);
    }

    @Override
    public PointOfSaleListDTO getPointOfSalesList(String merchantId, String type, String city, String address, String contactName, Pageable pageable) {

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

    private void checkViolations(List<PointOfSaleDTO> pointOfSaleDTOS){
        pointOfSaleDTOS.forEach(pointOfSaleDTO -> {
            Set<ConstraintViolation<PointOfSaleDTO>> violations = validator.validate(pointOfSaleDTO, ValidationApiEnabledGroup.class);
            if( !violations.isEmpty()){
                String errorMessages = violations.stream()
                        .map(ConstraintViolation::getMessage)
                        .collect(Collectors.joining("; "));
                throw new ClientExceptionWithBody(
                        HttpStatus.BAD_REQUEST,
                        "POINT_OF_SALE_BAD_REQUEST",
                        errorMessages);
                    }
        });
    }

}
