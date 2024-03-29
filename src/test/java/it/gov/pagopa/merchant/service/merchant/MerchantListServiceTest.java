package it.gov.pagopa.merchant.service.merchant;

import it.gov.pagopa.merchant.dto.MerchantDTO;
import it.gov.pagopa.merchant.dto.MerchantListDTO;
import it.gov.pagopa.merchant.model.Merchant;
import it.gov.pagopa.merchant.repository.MerchantRepository;
import it.gov.pagopa.merchant.test.fakers.MerchantFaker;
import it.gov.pagopa.common.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MerchantListServiceTest {

  @Mock private MerchantRepository repositoryMock;
  private final String FISCAL_CODE = "FISCAL_CODE";

  MerchantListService service;

  @BeforeEach
  void setUp() {
    service = new MerchantListServiceImpl(
            repositoryMock);
  }

  @Test
  void getMerchantList() {
    Merchant merchant1 = MerchantFaker.mockInstance(1);
    Merchant merchant2 = MerchantFaker.mockInstance(1);
    merchant2.setBusinessName("NAME_2");
    merchant2.setFiscalCode("FISCAL_CODE_2");
    merchant2.getInitiativeList().get(0).setUpdateDate(null);
    when(repositoryMock.findByFilter(Mockito.any(), Mockito.any())).thenReturn(List.of(merchant1, merchant2));

    MerchantDTO merchantDTO1 = MerchantDTO.builder()
            .merchantId(merchant1.getMerchantId())
            .businessName(merchant1.getBusinessName())
            .fiscalCode(merchant1.getFiscalCode())
            .merchantStatus("STATUS")
            .updateStatusDate(LocalDateTime.of(2023,5,22,10, 0).toString()).build();
    MerchantDTO merchantDTO2 = MerchantDTO.builder()
            .merchantId(merchant2.getMerchantId())
            .businessName(merchant2.getBusinessName())
            .fiscalCode(merchant2.getFiscalCode())
            .merchantStatus("STATUS")
            .updateStatusDate("").build();
    MerchantListDTO merchantListDTO_expected = MerchantListDTO.builder().content(List.of(merchantDTO1, merchantDTO2))
            .pageSize(15).totalElements(2).totalPages(1).build();

    MerchantListDTO result = service.getMerchantList("ORGANIZATION_ID_1", "INITIATIVEID1", FISCAL_CODE, null);

    assertEquals(2, result.getContent().size());
    assertEquals(merchantListDTO_expected, result);
    TestUtils.checkNotNullFields(result);
  }

  @Test
  void getMerchantList_empty() {
    when(repositoryMock.findByFilter(Mockito.any(), Mockito.any())).thenReturn(Collections.emptyList());

    MerchantListDTO merchantListDTO_expected = MerchantListDTO.builder().content(Collections.emptyList())
            .pageSize(15).totalElements(0).totalPages(0).build();

    MerchantListDTO result = service.getMerchantList("ORGANIZATION_ID_1","INITIATIVE_ID_1",  FISCAL_CODE, null);

    assertEquals(0, result.getContent().size());
    assertEquals(merchantListDTO_expected, result);
    TestUtils.checkNotNullFields(result);
  }
}
