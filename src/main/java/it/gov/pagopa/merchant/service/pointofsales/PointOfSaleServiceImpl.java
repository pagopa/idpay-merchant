package it.gov.pagopa.merchant.service.pointofsales;

import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleDTO;
import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleFilteredDTO;
import it.gov.pagopa.merchant.dto.pointofsales.PointOfSalePage;
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
        List<PointOfSale> pointOfSales = pointOfSaleDTOList.stream()
                .map(pointOfSaleDTO -> pointOfSaleDTOMapper.pointOfSaleDTOtoPointOfSaleEntity(pointOfSaleDTO,merchantId))
                .toList();
        pointOfSaleRepository.saveAll(pointOfSales);
    }

    @Override
    public PointOfSalePage getPointOfSales(PointOfSaleFilteredDTO filteredDTO){
        //TO-DO

        return null;
    }

    private Criteria buildCriteria(PointOfSaleFilteredDTO filteredDTO) {
        Criteria criteria = Criteria.where(PointOfSale.Fields.merchantId).is(filteredDTO.getMerchantId());

        if (filteredDTO.getType() != null) {
            criteria.and(PointOfSale.Fields.type).is(filteredDTO.getType());
        }
        if (filteredDTO.getCity() != null) {
            criteria.and(PointOfSale.Fields.city).is(filteredDTO.getCity());
        }
        if (filteredDTO.getAddress() != null) {
            criteria.and(PointOfSale.Fields.address).is(filteredDTO.getAddress());
        }
        if (filteredDTO.getContactName() != null) {
            criteria.and(PointOfSale.Fields.contactName).is(filteredDTO.getContactName());
        }

        return criteria;
    }

}
