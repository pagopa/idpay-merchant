package it.gov.pagopa.merchant.service.point_of_sale;

import it.gov.pagopa.merchant.dto.PointOfSaleDTO;
import it.gov.pagopa.merchant.dto.PointOfSaleListDTO;
import it.gov.pagopa.merchant.mapper.PointOfSaleMapper;
import it.gov.pagopa.merchant.model.PointOfSale;
import it.gov.pagopa.merchant.repository.PointOfSaleRepository;
import it.gov.pagopa.merchant.utils.Utilities;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PointOfSaleServiceImpl implements PointOfSaleService {

    private final PointOfSaleRepository pointOfSaleRepository;
    private final PointOfSaleMapper pointOfSaleMapper;

    public PointOfSaleServiceImpl(PointOfSaleRepository pointOfSaleRepository, PointOfSaleMapper pointOfSaleMapper) {
        this.pointOfSaleRepository = pointOfSaleRepository;
        this.pointOfSaleMapper = pointOfSaleMapper;
    }

    @Override
    public PointOfSaleListDTO getPointOfSalesList(String merchantId, String type, String city, String address, String contactName, Pageable pageable) {

        Criteria criteria = pointOfSaleRepository.getCriteria(merchantId, type, city, address, contactName);

        List<PointOfSale> entities = pointOfSaleRepository.findByFilter(criteria, pageable);
        long count = pointOfSaleRepository.getCount(criteria);

        final Page<PointOfSale> entitiesPage = PageableExecutionUtils.getPage(entities,
                Utilities.getPageable(pageable), () -> count);

        Page<PointOfSaleDTO> result = entitiesPage.map(pointOfSaleMapper::pointOfSaleEntityToPointOfSaleDTO);

        return PointOfSaleListDTO.builder()
                .content(result.getContent())
                .pageNo(result.getNumber())
                .pageSize(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .build();
    }
}
