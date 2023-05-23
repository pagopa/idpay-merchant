package it.gov.pagopa.merchant.service;

import it.gov.pagopa.merchant.dto.MerchantDetailDTO;
import it.gov.pagopa.merchant.dto.MerchantListDTO;
import it.gov.pagopa.merchant.service.merchant.MerchantDetailService;
import it.gov.pagopa.merchant.service.merchant.MerchantListService;
import org.springframework.data.domain.Pageable;
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
public class MerchantServiceImpl implements MerchantService{

    private final MerchantDetailService merchantDetailService;
    private final MerchantListService merchantListService;
    private final MerchantRepository merchantRepository;
    private final Initiative2InitiativeDTOMapper initiative2InitiativeDTOMapper;

    public MerchantServiceImpl(
            MerchantDetailService merchantDetailService,
            MerchantListService merchantListService,
            MerchantRepository merchantRepository,
            Initiative2InitiativeDTOMapper initiative2InitiativeDTOMapper) {
        this.merchantDetailService = merchantDetailService;
        this.merchantListService = merchantListService;
        this.merchantRepository = merchantRepository;
        this.initiative2InitiativeDTOMapper = initiative2InitiativeDTOMapper;
    }
    @Override
    public MerchantDetailDTO getMerchantDetail(String initiativeId,
                                               String merchantId) {
        return merchantDetailService.getMerchantDetail(initiativeId, merchantId);
    }
    @Override
    public MerchantListDTO getMerchantList(String initiativeId,
                                           String fiscalCode,
                                           Pageable pageable) {
        return merchantListService.getMerchantList(initiativeId, fiscalCode, pageable);
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
