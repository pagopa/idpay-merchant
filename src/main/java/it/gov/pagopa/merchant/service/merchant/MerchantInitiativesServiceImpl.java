package it.gov.pagopa.merchant.service.merchant;

import it.gov.pagopa.merchant.constants.MerchantConstants;
import it.gov.pagopa.merchant.dto.InitiativeDTO;
import it.gov.pagopa.merchant.mapper.Initiative2InitiativeDTOMapper;
import it.gov.pagopa.merchant.exception.ClientExceptionWithBody;
import it.gov.pagopa.merchant.model.Merchant;
import it.gov.pagopa.merchant.repository.MerchantRepository;
import org.springframework.http.HttpStatus;

import java.util.List;

public class MerchantInitiativesServiceImpl implements MerchantInitiativesService {

    private final MerchantRepository merchantRepository;
    private final Initiative2InitiativeDTOMapper initiative2InitiativeDTOMapper;

    public MerchantInitiativesServiceImpl(MerchantRepository merchantRepository, Initiative2InitiativeDTOMapper initiative2InitiativeDTOMapper) {
        this.merchantRepository = merchantRepository;
        this.initiative2InitiativeDTOMapper = initiative2InitiativeDTOMapper;
    }

    @Override
    public List<InitiativeDTO> getMerchantInitiativeList(String merchantId) {
        Merchant merchant = merchantRepository.findByMerchantId(merchantId)
                .orElseThrow(() -> new ClientExceptionWithBody(
                        HttpStatus.NOT_FOUND,
                        MerchantConstants.NOT_FOUND,
                        String.format(MerchantConstants.MERCHANT_BY_MERCHANT_ID_MESSAGE, merchantId)));

        return merchant.getInitiativeList().stream()
                .map(initiative2InitiativeDTOMapper)
                .toList();
    }
}
