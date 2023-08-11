package it.gov.pagopa.merchant.controller.merchant_portal;

import it.gov.pagopa.common.mongo.MongoTestUtilitiesService;
import it.gov.pagopa.common.web.mockvc.MockMvcUtils;
import it.gov.pagopa.merchant.BaseIntegrationTest;
import it.gov.pagopa.merchant.dto.MerchantDetailDTO;
import it.gov.pagopa.merchant.mapper.MerchantModelToDTOMapper;
import it.gov.pagopa.merchant.model.Merchant;
import it.gov.pagopa.merchant.repository.MerchantRepository;
import it.gov.pagopa.merchant.test.fakers.MerchantFaker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

class MerchantPortalMerchantControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private MerchantModelToDTOMapper merchantMapper;

    private final Set<String> merchantTestIds = new HashSet<>();

    @AfterEach
    void clearTestData() {
        merchantRepository.deleteAllById(merchantTestIds);
    }

//region API invokes
    protected MvcResult getMerchantDetail(String merchantId, String initiativeId) throws Exception {
        return mockMvc
                .perform(get("/idpay/merchant/portal/initiatives/{initiativeId}", initiativeId)
                        .header("x-merchant-id", merchantId)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andReturn();
    }
//endregion

    @Test
    void testGetMerchantDetail() throws Exception {
        // Given
        int N = 10;
        List<Merchant> storedMerchants = IntStream.range(0, N)
                .mapToObj(this::buildAndStoreMerchant)
                .toList();

        MongoTestUtilitiesService.startMongoCommandListener("retrieveMerchantId");
        for (Merchant expectedMerchant : storedMerchants) {
            // When
            String initiativeId = expectedMerchant.getInitiativeList().get(0).getInitiativeId();
            MerchantDetailDTO result = MockMvcUtils.extractResponse(getMerchantDetail(expectedMerchant.getMerchantId(), initiativeId), HttpStatus.OK, MerchantDetailDTO.class);

            // Then
            Assertions.assertEquals(
                    merchantMapper.toMerchantDetailDTO(expectedMerchant,initiativeId),
                    result);
        }
        MongoTestUtilitiesService.stopAndPrintMongoCommands();
    }

    private Merchant buildAndStoreMerchant(int bias) {
        Merchant stored = merchantRepository.save(MerchantFaker.mockInstance(bias));
        merchantTestIds.add(stored.getMerchantId());
        return stored;
    }
}
