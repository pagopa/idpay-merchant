package it.gov.pagopa.merchant.mapper;

import it.gov.pagopa.merchant.dto.MerchantDetailDTO;
import it.gov.pagopa.merchant.model.Merchant;
import it.gov.pagopa.merchant.test.fakers.MerchantDetailDTOFaker;
import it.gov.pagopa.merchant.test.fakers.MerchantFaker;
import it.gov.pagopa.merchant.test.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MerchantModelToDTOMapperTest {

 private MerchantModelToDTOMapper mapper;

 @BeforeEach
 void setUp() {
   mapper = new MerchantModelToDTOMapper();
 }

 @Test
 void merchantMapperTest(){
     MerchantDetailDTO merchantDetailDTO = MerchantDetailDTOFaker.mockInstance(1);
     Merchant merchant = MerchantFaker.mockInstance(1);

     MerchantDetailDTO result = mapper.toMerchantDetailDTO(merchant, merchantDetailDTO.getInitiativeId());
     assertAll(() -> {
     assertNotNull(result);
     TestUtils.checkNotNullFields(result);
   });
 }
}
