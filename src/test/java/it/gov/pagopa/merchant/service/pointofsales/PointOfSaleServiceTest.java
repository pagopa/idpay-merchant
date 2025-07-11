package it.gov.pagopa.merchant.service.pointofsales;

import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleDTO;
import it.gov.pagopa.merchant.mapper.PointOfSaleDTOMapper;
import it.gov.pagopa.merchant.model.PointOfSale;
import it.gov.pagopa.merchant.repository.PointOfSaleRepository;
import it.gov.pagopa.merchant.test.fakers.PointOfSaleDTOFaker;
import it.gov.pagopa.merchant.test.fakers.PointOfSaleFaker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PointOfSaleServiceTest {

  @Mock private PointOfSaleRepository repositoryMock;
  @Mock private final PointOfSaleDTOMapper dtoMapper = new PointOfSaleDTOMapper();

  private final String MERCHANT_ID = "MERCHANT_ID";

  PointOfSaleService service;

  @BeforeEach
  void setUp() {
    service = new PointOfSaleServiceImpl(
            repositoryMock,
            dtoMapper);
  }

  @Test
  void savePointOfSalesOK(){
    PointOfSaleDTO pointOfSaleDTO = PointOfSaleDTOFaker.mockInstance();
    PointOfSale pointOfSale = PointOfSaleFaker.mockInstance();

    when(repositoryMock.saveAll(any())).thenReturn(List.of(pointOfSale));

    service.savePointOfSales(MERCHANT_ID,List.of(pointOfSaleDTO));

    Mockito.verify(repositoryMock, Mockito.times(0)).saveAll(List.of(pointOfSale));

  }

}
