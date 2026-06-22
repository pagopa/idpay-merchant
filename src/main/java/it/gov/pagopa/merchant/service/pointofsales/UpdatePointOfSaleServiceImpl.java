package it.gov.pagopa.merchant.service.pointofsales;

import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleDTO;
import it.gov.pagopa.merchant.model.PointOfSale;
import it.gov.pagopa.merchant.repository.PointOfSaleRepository;
import it.gov.pagopa.merchant.utils.validator.PointOfSaleValidator;
import org.springframework.stereotype.Service;

@Service
public class UpdatePointOfSaleServiceImpl implements UpdatePointOfSaleService {

    private final GetPointOfSaleService getPointOfSaleService;
    private final PointOfSaleRepository pointOfSaleRepository;
    private final PointOfSaleValidator pointOfSaleValidator;

    public UpdatePointOfSaleServiceImpl(
            GetPointOfSaleService getPointOfSaleService,
            PointOfSaleRepository pointOfSaleRepository,
            PointOfSaleValidator pointOfSaleValidator) {
        this.getPointOfSaleService = getPointOfSaleService;
        this.pointOfSaleRepository = pointOfSaleRepository;
        this.pointOfSaleValidator = pointOfSaleValidator;
    }

    @Override
    public PointOfSale patchPointOfSale(String pointOfSaleId, String merchantId,
            PointOfSaleDTO pointOfSaleDTO) {
        pointOfSaleValidator.validatePointOfSalePatch(pointOfSaleDTO);

        PointOfSale pointOfSale = getPointOfSaleService.getPointOfSaleByIdAndMerchantId(
                pointOfSaleId, merchantId);

        applyPatch(pointOfSale, pointOfSaleDTO);
        return pointOfSaleRepository.save(pointOfSale);
    }

    private void applyPatch(PointOfSale pointOfSale, PointOfSaleDTO patch) {
        if (patch.getType() != null) {
            pointOfSale.setType(patch.getType().name());
        }
        if (patch.getFranchiseName() != null) {
            pointOfSale.setFranchiseName(patch.getFranchiseName());
        }
        if (patch.getRegion() != null) {
            pointOfSale.setRegion(patch.getRegion());
        }
        if (patch.getProvince() != null) {
            pointOfSale.setProvince(patch.getProvince());
        }
        if (patch.getCity() != null) {
            pointOfSale.setCity(patch.getCity());
        }
        if (patch.getZipCode() != null) {
            pointOfSale.setZipCode(patch.getZipCode());
        }
        if (patch.getAddress() != null) {
            pointOfSale.setAddress(patch.getAddress());
        }
        if (patch.getStreetNumber() != null) {
            pointOfSale.setStreetNumber(patch.getStreetNumber());
        }
        if (patch.getWebsite() != null) {
            pointOfSale.setWebsite(patch.getWebsite());
        }
        if (patch.getContactEmail() != null) {
            pointOfSale.setContactEmail(patch.getContactEmail());
        }
        if (patch.getContactName() != null) {
            pointOfSale.setContactName(patch.getContactName());
        }
        if (patch.getContactSurname() != null) {
            pointOfSale.setContactSurname(patch.getContactSurname());
        }
        if (patch.getChannelEmail() != null) {
            pointOfSale.setChannelEmail(patch.getChannelEmail());
        }
        if (patch.getChannelPhone() != null) {
            pointOfSale.setChannelPhone(patch.getChannelPhone());
        }
        if (patch.getChannelGeolink() != null) {
            pointOfSale.setChannelGeolink(patch.getChannelGeolink());
        }
        if (patch.getChannelWebsite() != null) {
            pointOfSale.setChannelWebsite(patch.getChannelWebsite());
        }
    }
}
