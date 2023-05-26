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
    private final Utilities utilities;

    public MerchantDetailServiceImpl(MerchantRepository merchantRepository, MerchantModelToDTOMapper merchantModelToDTOMapper, Utilities utilities) {
        this.merchantRepository = merchantRepository;
        this.merchantModelToDTOMapper = merchantModelToDTOMapper;
        this.utilities = utilities;
    }

    @Override
    public MerchantDetailDTO getMerchantDetail(String organizationId, String initiativeId, String merchantId) {
        long startTime = System.currentTimeMillis();
        log.info("[GET_MERCHANT_DETAIL] Get merchant with id {} for initiative {}", merchantId, initiativeId);

        Merchant merchantDetail = merchantRepository.retrieveByInitiativeIdAndMerchantId(initiativeId, organizationId, merchantId)
                .orElseThrow(() -> new ClientExceptionWithBody(HttpStatus.NOT_FOUND,
                        MerchantConstants.NOT_FOUND,
                        String.format(MerchantConstants.INITIATIVE_AND_MERCHANT_NOT_FOUND, initiativeId, merchantId)));

        utilities.performanceLog(startTime, "GET_MERCHANT_DETAIL");
        return merchantModelToDTOMapper.toMerchantDetailDTO(merchantDetail, initiativeId);
    }
}