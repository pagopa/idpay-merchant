package it.gov.pagopa.merchant.service.merchant;

import it.gov.pagopa.merchant.constants.MerchantConstants;
import it.gov.pagopa.merchant.dto.QueueInitiativeDTO;
import it.gov.pagopa.merchant.model.Merchant;
import it.gov.pagopa.merchant.repository.MerchantRepository;
import it.gov.pagopa.merchant.utils.Utilities;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class MerchantUpdatingInitiativeServiceImpl implements MerchantUpdatingInitiativeService{

    private final MerchantRepository merchantRepository;

    public MerchantUpdatingInitiativeServiceImpl(MerchantRepository merchantRepository) {
        this.merchantRepository = merchantRepository;
    }

    @Override
    public void updatingInitiative(QueueInitiativeDTO queueInitiativeDTO) {

        if ("DISCOUNT".equals(queueInitiativeDTO.getInitiativeRewardType()) && MerchantConstants.INITIATIVE_PUBLISHED.equals(queueInitiativeDTO.getStatus())) {
            long startTime = System.currentTimeMillis();

            List<Merchant> merchantList = merchantRepository.retrieveByInitiativeId(queueInitiativeDTO.getInitiativeId());


            merchantList.forEach(merchant -> merchantRepository.updateInitiativeOnMerchant(merchant, queueInitiativeDTO.getInitiativeId()));

            log.info("[UPDATE_INITIATIVE] Updated initiative {} to status published", queueInitiativeDTO.getInitiativeId());

            Utilities.performanceLog(startTime, "UPDATE_INITIATIVE");
        }
    }
}
