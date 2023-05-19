package it.gov.pagopa.merchant.service;

import it.gov.pagopa.merchant.constants.MerchantConstants;
import it.gov.pagopa.merchant.dto.InitiativeDTO;
import it.gov.pagopa.merchant.dto.mapper.Initiative2InitiativeDTOMapper;
import it.gov.pagopa.merchant.exception.MerchantException;
import it.gov.pagopa.merchant.model.Merchant;
import it.gov.pagopa.merchant.repository.MerchantRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MerchantServiceImpl implements MerchantService {

    private final MerchantRepository merchantRepository;

    private final Initiative2InitiativeDTOMapper initiative2InitiativeDTOMapper;

    public MerchantServiceImpl(MerchantRepository merchantRepository, Initiative2InitiativeDTOMapper initiative2InitiativeDTOMapper) {
        this.merchantRepository = merchantRepository;
        this.initiative2InitiativeDTOMapper = initiative2InitiativeDTOMapper;
    }

    @Override
    public List<InitiativeDTO> getMerchantInitiativeList(String merchantId) {
        Merchant merchant = merchantRepository.findByMerchantId(merchantId)
                .orElseThrow(() -> new MerchantException(
                        MerchantConstants.Exception.NotFound.CODE,
                        String.format(
                                MerchantConstants.Exception.NotFound.MERCHANT_BY_MERCHANT_ID_MESSAGE,
                                merchantId),
                        HttpStatus.NOT_FOUND));

        return merchant.getInitiativeList().stream()
                .map(initiative2InitiativeDTOMapper)
                .toList();
    }
}
