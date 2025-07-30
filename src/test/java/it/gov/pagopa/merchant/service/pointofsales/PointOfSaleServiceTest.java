package it.gov.pagopa.merchant.service.pointofsales;

import com.mongodb.MongoException;
import it.gov.pagopa.common.web.exception.ServiceException;
import it.gov.pagopa.merchant.dto.MerchantDetailDTO;
import it.gov.pagopa.merchant.exception.custom.MerchantNotFoundException;
import it.gov.pagopa.merchant.exception.custom.PointOfSaleDuplicateException;
import it.gov.pagopa.merchant.exception.custom.PointOfSaleNotFoundException;
import it.gov.pagopa.merchant.model.PointOfSale;
import it.gov.pagopa.merchant.repository.PointOfSaleRepository;
import it.gov.pagopa.merchant.service.MerchantService;
import it.gov.pagopa.merchant.test.fakers.MerchantDetailDTOFaker;
import it.gov.pagopa.merchant.test.fakers.PointOfSaleFaker;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointOfSaleServiceTest {

  @Mock private MerchantService merchantServiceMock;
  @Mock private PointOfSaleRepository repositoryMock;

  private static final String MERCHANT_ID = "MERCHANT_ID";

  PointOfSaleService service;

  @BeforeEach
  void setUp() {
    service = new PointOfSaleServiceImpl(
            merchantServiceMock,
            repositoryMock);
  }

  @Test
  void savePointOfSalesOK(){
    PointOfSale pointOfSale = PointOfSaleFaker.mockInstance();
    MerchantDetailDTO merchantDetailDTOFaker = MerchantDetailDTOFaker.mockInstance(1);
    when(merchantServiceMock.getMerchantDetail(anyString())).thenReturn(merchantDetailDTOFaker);
    when(repositoryMock.save(any())).thenReturn(pointOfSale);
    when(repositoryMock.findById(any())).thenReturn(Optional.of(pointOfSale));
    service.savePointOfSales(MERCHANT_ID,List.of(pointOfSale));

    Mockito.verify(repositoryMock, Mockito.times(1)).save(pointOfSale);
  }

  @Test
  void savePointOfSalesOK_withIdNull(){
    PointOfSale pointOfSale = PointOfSaleFaker.mockInstance();
    pointOfSale.setId(null);
    MerchantDetailDTO merchantDetailDTOFaker = MerchantDetailDTOFaker.mockInstance(1);
    when(merchantServiceMock.getMerchantDetail(anyString())).thenReturn(merchantDetailDTOFaker);
    when(repositoryMock.save(any())).thenReturn(pointOfSale);
    service.savePointOfSales(MERCHANT_ID,List.of(pointOfSale));

    Mockito.verify(repositoryMock, Mockito.times(1)).save(pointOfSale);

  }


  @Test
  void savePointOfSalesOK_withId(){
    PointOfSale pointOfSale = PointOfSaleFaker.mockInstance();
    pointOfSale.setId(new ObjectId());
    MerchantDetailDTO merchantDetailDTOFaker = MerchantDetailDTOFaker.mockInstance(1);
    when(merchantServiceMock.getMerchantDetail(anyString())).thenReturn(merchantDetailDTOFaker);
    when(repositoryMock.save(any())).thenReturn(pointOfSale);
    when(repositoryMock.findById(any())).thenReturn(Optional.of(pointOfSale));
    service.savePointOfSales(MERCHANT_ID,List.of(pointOfSale));

    Mockito.verify(repositoryMock, Mockito.times(1)).save(pointOfSale);

  }

  @Test
  void savePointOfSalesOK_withIdNotFound(){
    PointOfSale pointOfSale = PointOfSaleFaker.mockInstance();
    pointOfSale.setId(new ObjectId());
    MerchantDetailDTO merchantDetailDTOFaker = MerchantDetailDTOFaker.mockInstance(1);
    when(merchantServiceMock.getMerchantDetail(anyString())).thenReturn(merchantDetailDTOFaker);
    when(repositoryMock.findById(any())).thenReturn(Optional.empty());
    List<PointOfSale> pointOfSales = new ArrayList<>();
    pointOfSales.add(pointOfSale);
    assertThrows(PointOfSaleNotFoundException.class, () -> callSave(pointOfSales));
  }


  @Test
  void savePointOfSalesKO_genericError(){
    PointOfSale pointOfSale1 = PointOfSaleFaker.mockInstance();
    PointOfSale pointOfSale2 = PointOfSaleFaker.mockInstance();
    List<PointOfSale> pointOfSales = new ArrayList<>();
    pointOfSales.add(pointOfSale1);
    pointOfSales.add(pointOfSale2);
    MerchantDetailDTO merchantDetailDTOFaker = MerchantDetailDTOFaker.mockInstance(1);
    when(merchantServiceMock.getMerchantDetail(anyString())).thenReturn(merchantDetailDTOFaker);
    when(repositoryMock.findById(any())).thenReturn(Optional.of(pointOfSale1));
    when(repositoryMock.findById(any())).thenReturn(Optional.of(pointOfSale2));
    when(repositoryMock.save(pointOfSale1)).thenReturn(pointOfSale1);
    when(repositoryMock.save(pointOfSale2)).thenThrow(new MongoException("DUMMY_EXCEPTION"));
    Mockito.doThrow(new MongoException("Command error dummy")).when(repositoryMock).deleteById(any());
    assertThrows(ServiceException.class, () -> callSave(pointOfSales));
  }

  @Test
  void savePointOfSalesKO_genericErrorCompensatingOk(){
    PointOfSale pointOfSale1 = PointOfSaleFaker.mockInstance();
    PointOfSale pointOfSale2 = PointOfSaleFaker.mockInstance();
    List<PointOfSale> pointOfSales = new ArrayList<>();
    pointOfSales.add(pointOfSale1);
    pointOfSales.add(pointOfSale2);
    MerchantDetailDTO merchantDetailDTOFaker = MerchantDetailDTOFaker.mockInstance(1);
    when(merchantServiceMock.getMerchantDetail(anyString())).thenReturn(merchantDetailDTOFaker);
    when(repositoryMock.findById(any())).thenReturn(Optional.of(pointOfSale1));
    when(repositoryMock.findById(any())).thenReturn(Optional.of(pointOfSale2));
    when(repositoryMock.save(pointOfSale1)).thenReturn(pointOfSale1);
    when(repositoryMock.save(pointOfSale2)).thenThrow(new MongoException("DUMMY_EXCEPTION"));
    doNothing().when(repositoryMock).deleteById(any());
    assertThrows(ServiceException.class, () -> callSave(pointOfSales));
  }

  @Test
  void savePointOfSalesKO_duplicateKeyException(){
    PointOfSale pointOfSale = PointOfSaleFaker.mockInstance();
    MerchantDetailDTO merchantDetailDTOFaker = MerchantDetailDTOFaker.mockInstance(1);
    when(merchantServiceMock.getMerchantDetail(anyString())).thenReturn(merchantDetailDTOFaker);
    when(repositoryMock.findById(any())).thenReturn(Optional.of(pointOfSale));
    DuplicateKeyException duplicateKeyException = mock(DuplicateKeyException.class);
    when(repositoryMock.save(any())).thenThrow(duplicateKeyException);
    assertThrows(PointOfSaleDuplicateException.class, () -> callSave(pointOfSale));
  }

  @Test
  void savePointOfSalesKO_merchantIsNull(){
    PointOfSale pointOfSale = PointOfSaleFaker.mockInstance();
    when(merchantServiceMock.getMerchantDetail(anyString())).thenReturn(null);

    assertThrows(MerchantNotFoundException.class, () -> callSave(pointOfSale));
  }


  private void callSave(List<PointOfSale> dtos){
    service.savePointOfSales(MERCHANT_ID,dtos);
  }

  private void callSave(PointOfSale dto){
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
    Page<PointOfSale> pointOfSalePage = service.getPointOfSalesList(MERCHANT_ID,"type","city","address","contactName",paging);
    assertNotNull(pointOfSalePage);
  }

}
