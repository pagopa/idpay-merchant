package it.gov.pagopa.merchant.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import it.gov.pagopa.merchant.dto.MerchantCreateDTO;
import it.gov.pagopa.merchant.model.Merchant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(classes = {MerchantCreateDTOMapper.class})
@ExtendWith(SpringExtension.class)
class MerchantCreateDTOMapperTest {

  @Autowired
  private MerchantCreateDTOMapper merchantCreateDTOMapper;

  @Test
  void testDtoToEntity_NullDto() {
    Merchant result = merchantCreateDTOMapper.dtoToEntity(null, "merchantId");
    assertNull(result, "Expected null when DTO is null");
  }

  @Test
  void testDtoToEntity_BlankMerchantId() {
    MerchantCreateDTO dto = new MerchantCreateDTO();
    Merchant result = merchantCreateDTOMapper.dtoToEntity(dto, "  ");
    assertNull(result, "Expected null when merchantId is blank");
  }

  @Test
  void testDtoToEntity_ValidInput() {
    MerchantCreateDTO dto = new MerchantCreateDTO();
    dto.setFiscalCode("ABC12345678");
    dto.setAcquirerId("Acquirer123");
    dto.setBusinessName("Test Business");
    dto.setIban("IT60X0542811101000000123456");
    dto.setIbanHolder("Holder Name");

    Merchant result = merchantCreateDTOMapper.dtoToEntity(dto, "merchantId");

    assertNotNull(result, "Expected non-null Merchant entity");
    assertEquals("merchantId", result.getMerchantId());
    assertEquals("ABC12345678", result.getFiscalCode());
    assertEquals("Acquirer123", result.getAcquirerId());
    assertEquals("Test Business", result.getBusinessName());
    assertEquals("IT60X0542811101000000123456", result.getIban());
    assertEquals("Holder Name", result.getIbanHolder());
  }

  @Test
  void testDtoToEntity_EmptyMerchantId() {
    MerchantCreateDTO dto = new MerchantCreateDTO();
    dto.setFiscalCode("ABC12345678");
    Merchant result = merchantCreateDTOMapper.dtoToEntity(dto, "");
    assertNull(result, "Expected null when merchantId is empty");
  }
}
