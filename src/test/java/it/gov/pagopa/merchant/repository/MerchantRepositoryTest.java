package it.gov.pagopa.merchant.repository;

import it.gov.pagopa.merchant.BaseIntegrationTest;
import it.gov.pagopa.merchant.model.Merchant;
import it.gov.pagopa.merchant.test.fakers.MerchantFaker;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class MerchantRepositoryTest extends BaseIntegrationTest {

    @Autowired
    protected MerchantRepository merchantRepository;

    @Test
    void findByAcquirerIdAndFiscalCode() {
        Merchant merchant = MerchantFaker.mockInstance(1);

        merchantRepository.save(merchant);

        Merchant byAcquirerIdAndFiscalCode = merchantRepository.findByAcquirerIdAndFiscalCode(merchant.getAcquirerId(), merchant.getFiscalCode()).orElse(null);

        System.out.println(byAcquirerIdAndFiscalCode);
        //TODO sistemare test
    }
}