package it.gov.pagopa.merchant.service.sale;

import it.gov.pagopa.merchant.dto.sale.PointOfSaleDTO;
import it.gov.pagopa.merchant.dto.sale.PointOfSaleFilteredDTO;
import it.gov.pagopa.merchant.mapper.PointOfSaleDTOMapper;
import it.gov.pagopa.merchant.model.PointOfSale;
import it.gov.pagopa.merchant.repository.PointOfSaleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class PointOfSaleServiceImpl implements PointOfSaleService {

    private final PointOfSaleRepository pointOfSaleRepository;
    private final PointOfSaleDTOMapper pointOfSaleDTOMapper;

    public PointOfSaleServiceImpl(
            PointOfSaleRepository pointOfSaleRepository,
            PointOfSaleDTOMapper pointOfSaleDTOMapper) {
        this.pointOfSaleRepository = pointOfSaleRepository;
        this.pointOfSaleDTOMapper = pointOfSaleDTOMapper;
    }

    @Override
    public void savePointOfSales(String merchantId, List<PointOfSaleDTO> pointOfSaleDTOList){
        pointOfSaleRepository.saveAll(pointOfSaleDTOList.stream().map(pointOfSaleDTO -> pointOfSaleDTOMapper.PointOfSaleDTOtoPointOfSaleEntity(pointOfSaleDTO,merchantId)).toList());
    }

    @Override
    public List<PointOfSaleDTO> getPointOfSales(PointOfSaleFilteredDTO filteredDTO){
        Criteria criteria = pointOfSaleRepository.getCriteria(filteredDTO.getMerchantId(), filteredDTO.getType(), filteredDTO.getCity(), filteredDTO.getAddress(), filteredDTO.getContactName());
        List<PointOfSale> onboardinglist = pointOfSaleRepository.findByFilter(criteria, filteredDTO.getPageable());
        return onboardinglist.stream().map(pointOfSaleDTOMapper::PointOfSaleEntityToPointOfSaleDTO).toList();
    }

}
