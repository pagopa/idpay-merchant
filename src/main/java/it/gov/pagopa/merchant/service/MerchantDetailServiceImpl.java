package it.gov.pagopa.merchant.service;

import it.gov.pagopa.merchant.constants.MerchantConstants;
import it.gov.pagopa.merchant.exception.MerchantException;
import it.gov.pagopa.merchant.dto.MerchantDetailDTO;
import it.gov.pagopa.merchant.mapper.MerchantModelToDTOMapper;
import it.gov.pagopa.merchant.model.Merchant;
import it.gov.pagopa.merchant.repository.MerchantRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class MerchantDetailServiceImpl implements MerchantDetailService {
    private final MerchantRepository merchantRepository;
    private final MerchantModelToDTOMapper merchantModelToDTOMapper;

    public MerchantDetailServiceImpl(MerchantRepository merchantRepository, MerchantModelToDTOMapper merchantModelToDTOMapper) {
        this.merchantRepository = merchantRepository;
        this.merchantModelToDTOMapper = merchantModelToDTOMapper;
    }

    @Override
    public MerchantDetailDTO getMerchantDetail(String initiativeId, String merchantId) {
        Merchant merchantDetail = merchantRepository.retrieveByInitiativeIdAndMerchantId(initiativeId, merchantId)
                .orElseThrow(() -> new MerchantException(MerchantConstants.Exception.NotFound.CODE,
                        String.format(MerchantConstants.Exception.NotFound.INITIATIVE_BY_INITIATIVE_AND_MERCHANT_ID_MESSAGE, initiativeId, merchantId),
                        HttpStatus.NOT_FOUND));
        return merchantModelToDTOMapper.toMerchantDetailDTO(merchantDetail, initiativeId);
    }
}