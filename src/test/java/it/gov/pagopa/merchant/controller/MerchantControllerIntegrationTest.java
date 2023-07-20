package it.gov.pagopa.merchant.controller;

import it.gov.pagopa.common.mongo.MongoTestUtilitiesService;
import it.gov.pagopa.common.web.mockvc.MockMvcUtils;
import it.gov.pagopa.merchant.BaseIntegrationTest;
import it.gov.pagopa.merchant.model.Merchant;
import it.gov.pagopa.merchant.repository.MerchantRepository;
import it.gov.pagopa.merchant.test.fakers.MerchantFaker;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

class MerchantControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MerchantRepository merchantRepository;

    private final Set<String> merchantTestIds = new HashSet<>();

    @AfterEach
    void clearTestData(){
        merchantRepository.deleteAllById(merchantTestIds);
    }

//region API invokes
    protected MvcResult retrieveMerchantId(String acquirerId, String merchantFiscalCode) throws Exception {
        return mockMvc
                .perform(get("/idpay/merchant/acquirer/{acquirerId}/merchant-fiscalcode/{fiscalCode}/id", acquirerId, merchantFiscalCode)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andReturn();
    }
//endregion

    @Test
    void testOnboardingCitizen() {
        // Given
        int N=10;
        Map<Pair<String, String>, String> expectedAcquirerIdMerchantFc2MerchantId = IntStream.range(0, N)
                .mapToObj(this::buildAndStoreMerchant)
                        .collect(Collectors.toMap(m -> Pair.of(m.getAcquirerId(), m.getFiscalCode()), Merchant::getMerchantId));

        // When
        MongoTestUtilitiesService.startMongoCommandListener("retrieveMerchantId");
        Map<Pair<String, String>, String> result = expectedAcquirerIdMerchantFc2MerchantId.keySet().stream()
                .collect(Collectors.toMap(Function.identity(),
                        aid2mfc -> {
                            try {
                                return Objects.requireNonNull(MockMvcUtils.extractResponse(retrieveMerchantId(aid2mfc.getKey(), aid2mfc.getValue()), HttpStatus.OK, String.class));
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }));
        MongoTestUtilitiesService.stopAndPrintMongoCommands();

        // Then
        Assertions.assertEquals(expectedAcquirerIdMerchantFc2MerchantId, result);
    }

    private Merchant buildAndStoreMerchant(int bias){
        Merchant stored = merchantRepository.save(MerchantFaker.mockInstance(bias));
        merchantTestIds.add(stored.getMerchantId());
        return stored;
    }

}
