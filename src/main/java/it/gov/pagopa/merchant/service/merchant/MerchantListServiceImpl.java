package it.gov.pagopa.merchant.service.merchant;

import it.gov.pagopa.merchant.dto.MerchantDTO;
import it.gov.pagopa.merchant.dto.MerchantListDTO;
import it.gov.pagopa.merchant.model.Merchant;
import it.gov.pagopa.merchant.repository.MerchantRepository;
import it.gov.pagopa.merchant.utils.Utilities;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class MerchantListServiceImpl implements MerchantListService {
    private final MerchantRepository merchantRepository;
    public static final String EMPTY = "";

    public MerchantListServiceImpl(MerchantRepository merchantRepository) {
        this.merchantRepository = merchantRepository;
    }

    @Override
    public MerchantListDTO getMerchantList(String organizationId, String initiativeId, String fiscalCode, Pageable pageable) {
        long startTime = System.currentTimeMillis();
        log.info("[GET_MERCHANT_LIST] Get merchant list for initiative {}", initiativeId);

        Criteria criteria = merchantRepository.getCriteria(initiativeId, organizationId, fiscalCode);
        List<Merchant> merchantModelList = merchantRepository.findByFilter(criteria, pageable);
        List<MerchantDTO> merchantDTOList = new ArrayList<>();
        if (!merchantModelList.isEmpty()) {
            merchantModelList.forEach(
                    merchant -> merchant.getInitiativeList().stream()
                            .filter(i -> i.getInitiativeId().equals(initiativeId)).findFirst().ifPresent(initiative -> merchantDTOList.add(
                                    new MerchantDTO(
                                            merchant.getMerchantId(),
                                            merchant.getBusinessName(),
                                            merchant.getFiscalCode(),
                                            initiative.getMerchantStatus(),
                                            initiative.getUpdateDate() != null ? initiative.getUpdateDate().toString() : EMPTY
                                    ))));
        }

        long count = merchantRepository.getCount(criteria);
        final Page<Merchant> result = PageableExecutionUtils.getPage(merchantModelList,
                Utilities.getPageable(pageable), () -> count);

        Utilities.performanceLog(startTime, "GET_MERCHANT_LIST");
        return new MerchantListDTO(merchantDTOList, result.getNumber(), result.getSize(),
                (int) result.getTotalElements(), result.getTotalPages());
    }

}
