package it.gov.pagopa.merchant.service.merchant;

import it.gov.pagopa.merchant.constants.MerchantConstants;
import it.gov.pagopa.merchant.dto.MerchantDetailDTO;
import it.gov.pagopa.merchant.exception.ClientExceptionWithBody;
import it.gov.pagopa.merchant.mapper.MerchantModelToDTOMapper;
import it.gov.pagopa.merchant.model.Merchant;
import it.gov.pagopa.merchant.repository.MerchantRepository;
import it.gov.pagopa.merchant.utils.Utilities;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MerchantDetailServiceImpl implements MerchantDetailService {
    private final MerchantRepository merchantRepository;
    private final MerchantModelToDTOMapper merchantModelToDTOMapper;

    public MerchantDetailServiceImpl(MerchantRepository merchantRepository, MerchantModelToDTOMapper merchantModelToDTOMapper) {
        this.merchantRepository = merchantRepository;
        this.merchantModelToDTOMapper = merchantModelToDTOMapper;
    }

    @Override
    public MerchantDetailDTO getMerchantDetail(String organizationId, String initiativeId, String merchantId) {
        long startTime = System.currentTimeMillis();
        log.info("[GET_MERCHANT_DETAIL] Get merchant with id {} for initiative {}", merchantId, initiativeId);

        Merchant merchantDetail = merchantRepository.retrieveByInitiativeIdAndOrganizationIdAndMerchantId(initiativeId, organizationId, merchantId)
                .orElseThrow(() -> new ClientExceptionWithBody(HttpStatus.NOT_FOUND,
                        MerchantConstants.NOT_FOUND,
                        String.format(MerchantConstants.INITIATIVE_AND_MERCHANT_NOT_FOUND, initiativeId, merchantId)));

        Utilities.performanceLog(startTime, "GET_MERCHANT_DETAIL");
        return merchantModelToDTOMapper.toMerchantDetailDTO(merchantDetail, initiativeId);
    }

    @Override
    public MerchantDetailDTO getMerchantDetail(String merchantId, String initiativeId) {
        return merchantRepository.retrieveByMerchantIdAndInitiativeId(merchantId, initiativeId)
                .map(merchant -> merchantModelToDTOMapper.toMerchantDetailDTO(merchant, initiativeId))
                .orElse(null);
    }
}