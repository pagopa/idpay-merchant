package it.gov.pagopa.merchant.service.merchant;

import it.gov.pagopa.merchant.constants.MerchantConstants;
import it.gov.pagopa.merchant.dto.QueueInitiativeDTO;
import it.gov.pagopa.merchant.repository.MerchantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MerchantUpdatingInitiativeServiceTest {

  @Mock private MerchantRepository repositoryMock;
  private static final String INITIATIVE_ID = "INITIATIVE_ID";
  MerchantUpdatingInitiativeService service;

  @BeforeEach
  void setUp() {
    service = new MerchantUpdatingInitiativeServiceImpl(
            repositoryMock);
  }

  @Test
  void updatingMerchantInitiativeRefundType() {
    QueueInitiativeDTO queueInitiativeDTO = QueueInitiativeDTO.builder()
            .initiativeId(INITIATIVE_ID)
            .initiativeRewardType("REFUND").build();

    service.updatingInitiative(queueInitiativeDTO);

    Mockito.verify(repositoryMock, Mockito.times(0)).updateInitiativeOnMerchant(INITIATIVE_ID);
  }
  @Test
  void updatingMerchantInitiativeNotPublished() {
    QueueInitiativeDTO queueInitiativeDTO = QueueInitiativeDTO.builder()
            .initiativeId(INITIATIVE_ID)
            .initiativeRewardType("DISCOUNT")
            .status("APPROVED").build();

    service.updatingInitiative(queueInitiativeDTO);

    Mockito.verify(repositoryMock, Mockito.times(0)).updateInitiativeOnMerchant(INITIATIVE_ID);
  }
  @Test
  void updatingMerchantInitiativeNotPublishedAndRefundType() {
    QueueInitiativeDTO queueInitiativeDTO = QueueInitiativeDTO.builder()
            .initiativeId(INITIATIVE_ID)
            .initiativeRewardType("REFUND")
            .status("APPROVED").build();

    service.updatingInitiative(queueInitiativeDTO);

    Mockito.verify(repositoryMock, Mockito.times(0)).updateInitiativeOnMerchant(INITIATIVE_ID);
  }
  @Test
  void updatingMerchantInitiative() {
    QueueInitiativeDTO queueInitiativeDTO = QueueInitiativeDTO.builder()
            .initiativeId(INITIATIVE_ID)
            .initiativeRewardType("DISCOUNT")
            .status(MerchantConstants.INITIATIVE_PUBLISHED).build();
    Mockito.doNothing().when(repositoryMock).updateInitiativeOnMerchant(INITIATIVE_ID);

    service.updatingInitiative(queueInitiativeDTO);

    Mockito.verify(repositoryMock, Mockito.times(1)).updateInitiativeOnMerchant(INITIATIVE_ID);
  }
}
