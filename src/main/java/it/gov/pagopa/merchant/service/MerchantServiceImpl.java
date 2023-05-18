package it.gov.pagopa.merchant.service;

import it.gov.pagopa.merchant.model.Initiative;
import it.gov.pagopa.merchant.model.Merchant;
import it.gov.pagopa.merchant.repository.MerchantRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MerchantServiceImpl implements MerchantService {

    private final MerchantRepository merchantRepository;

    public MerchantServiceImpl(MerchantRepository merchantRepository) {
        this.merchantRepository = merchantRepository;
    }

    @Override
    public List<Initiative> getMerchantInitiativeList(String merchantId, Boolean enabled) {
        Merchant merchant = merchantRepository.findByMerchantId(merchantId)
                .orElseThrow(); //TODO lanciare errore

        List<Initiative> initiativeList = merchant.getInitiativeList();
        if (enabled != null) {
            initiativeList = initiativeList.stream()
                    .filter(initiative -> initiative.isEnabled() == enabled)
                    .collect(Collectors.toList());
        }

        return initiativeList;
    }
}
