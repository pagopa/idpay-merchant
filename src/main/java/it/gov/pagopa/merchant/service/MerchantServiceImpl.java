package it.gov.pagopa.merchant.service;

import it.gov.pagopa.merchant.model.Merchant;
import it.gov.pagopa.merchant.repository.MerchantRepository;
import org.springframework.stereotype.Service;

@Service
public class MerchantServiceImpl implements MerchantService {
    private final MerchantRepository merchantRepository;

    public MerchantServiceImpl(MerchantRepository merchantRepository) {
        this.merchantRepository = merchantRepository;
    }

    @Override
    public String retrieveMerchantId(String fiscalCode, String acquirerId) {
        Merchant merchant = merchantRepository.findByFiscalCodeAndAcquirerId(fiscalCode, acquirerId);

        if(merchant == null){
            throw new RuntimeException();//TODO GESTIRE ECCEZIONE
        }

        if(!merchant.getFiscalCode().equals(fiscalCode) || !merchant.getAcquirerId().equals(acquirerId)){
           throw  new RuntimeException(); //TODO GESTIRE ECCEZIONE
        }

        return merchant.getMerchantId();
    }
}
