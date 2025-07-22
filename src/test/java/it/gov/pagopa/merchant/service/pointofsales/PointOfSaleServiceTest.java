package it.gov.pagopa.merchant.service.pointofsales;

import it.gov.pagopa.merchant.dto.MerchantDetailDTO;
import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleDTO;
import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleListDTO;
import it.gov.pagopa.merchant.exception.custom.MerchantNotFoundException;
import it.gov.pagopa.merchant.mapper.PointOfSaleDTOMapper;
import it.gov.pagopa.merchant.model.PointOfSale;
import it.gov.pagopa.merchant.repository.PointOfSaleRepository;
import it.gov.pagopa.merchant.service.MerchantService;
import it.gov.pagopa.merchant.test.fakers.MerchantDetailDTOFaker;
import it.gov.pagopa.merchant.test.fakers.PointOfSaleDTOFaker;
import it.gov.pagopa.merchant.test.fakers.PointOfSaleFaker;
import it.gov.pagopa.merchant.utils.validator.PointOfSaleValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PointOfSaleServiceTest {

  @Mock private MerchantService merchantServiceMock;
  @Mock private PointOfSaleRepository repositoryMock;
  @Mock private final PointOfSaleDTOMapper dtoMapper = new PointOfSaleDTOMapper();
  @Mock private PointOfSaleValidator validator;

  private static final String MERCHANT_ID = "MERCHANT_ID";

  PointOfSaleService service;

  @BeforeEach
  void setUp() {
    service = new PointOfSaleServiceImpl(
            merchantServiceMock,
            repositoryMock,
            dtoMapper,
            validator);
  }

  @Test
  void savePointOfSalesOK(){
    PointOfSaleDTO pointOfSaleDTO = PointOfSaleDTOFaker.mockInstance();
    PointOfSale pointOfSale = PointOfSaleFaker.mockInstance();
    MerchantDetailDTO merchantDetailDTOFaker = MerchantDetailDTOFaker.mockInstance(1);
    when(merchantServiceMock.getMerchantDetail(anyString())).thenReturn(merchantDetailDTOFaker);

    when(repositoryMock.saveAll(any())).thenReturn(List.of(pointOfSale));

    service.savePointOfSales(MERCHANT_ID,List.of(pointOfSaleDTO));

    Mockito.verify(repositoryMock, Mockito.times(0)).saveAll(List.of(pointOfSale));

  }

  @Test
  void savePointOfSalesKO(){
    PointOfSaleDTO pointOfSaleDTO = PointOfSaleDTOFaker.mockInstance();
    when(merchantServiceMock.getMerchantDetail(anyString())).thenReturn(null);

    assertThrows(MerchantNotFoundException.class, () -> callSave(pointOfSaleDTO));

  }

  private void callSave(PointOfSaleDTO dto){
    service.savePointOfSales(MERCHANT_ID,List.of(dto));
  }


  @Test
  void getPointOfSalesListOK(){
    PointOfSale pointOfSale = PointOfSaleFaker.mockInstance();
    MerchantDetailDTO merchantDetailDTOFaker = MerchantDetailDTOFaker.mockInstance(1);
    when(merchantServiceMock.getMerchantDetail(anyString())).thenReturn(merchantDetailDTOFaker);

    Criteria criteria = new Criteria();
    when(repositoryMock.getCriteria(any(),any(),any(),any(),any())).thenReturn(criteria);
    when(repositoryMock.findByFilter(any(),any())).thenReturn(List.of(pointOfSale));

    Pageable paging = PageRequest.of(0, 20, Sort.by("franchiseName"));
    PointOfSaleListDTO pointOfSaleListDTO = service.getPointOfSalesList(MERCHANT_ID,"type","city","address","contactName",paging);
    assertNotNull(pointOfSaleListDTO);
  }

}
