package oikos.app.buyer;

import oikos.app.common.exceptions.EntityNotFoundException;
import oikos.app.notifications.NotificationService;
import oikos.app.users.Role;
import oikos.app.users.User;
import oikos.app.users.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Created by Mohamed Haamdi on 26/04/2021. */
@ExtendWith(MockitoExtension.class)
class BuyerServiceTest {
  @Mock UserRepo repoMock;
  @Mock NotificationService notificationService;
  BuyerService buyerService;

  @BeforeEach
  void setUp() {
    buyerService = new BuyerService(repoMock, notificationService);
  }

  @Test
  void validateBuyerUserNotFound() {
    // Given
    String id = "INVALID_ID";
    // When
    when(repoMock.findById(id)).thenReturn(Optional.empty());
    // Then
    assertThrows(EntityNotFoundException.class, () -> buyerService.validateBuyer(id));
  }

  @Test
  void validateBuyerBuyerProfileNotFound() {
    // Given
    var id = "Valid_ID";
    var user = new User();
    user.setId(id);
    user.setRoles(new HashSet<>());
    user.setBuyerProfile(null);
    when(repoMock.findById(id)).thenReturn(Optional.of(user));
    when(repoMock.save(user)).thenReturn(user);
    // When
    var buyer = buyerService.validateBuyer(id);
    // Then
    assertThat(buyer.getBuyerProfile()).isNotNull();
    assertThat(buyer.getBuyerProfile().isValidated()).isTrue();
    assertThat(buyer.getRoles()).contains(Role.BUYER);
  }

  @Test
  void validateBuyerProfileFound() {
    // Given
    var id = "Valid_ID";
    var user = new User();
    user.setId(id);
    user.setBuyerProfile(new Buyer());
    when(repoMock.findById(id)).thenReturn(Optional.of(user));
    when(repoMock.save(user)).thenReturn(user);
    // When
    var buyer = buyerService.validateBuyer(id);
    // Then
    assertThat(buyer.getBuyerProfile().isValidated()).isTrue();
    verify(notificationService).addNotification(any(String.class), any(String.class), any());
  }

  @Test
  void canDoUserCan() {
    // Given
    var method = BuyerService.BuyerMethods.VALIDATE_BUYER;
    var userID = "ADMIN_ID";
    var user = new User();
    user.setRoles(Set.of(Role.ADMIN));
    when(repoMock.getOne(userID)).thenReturn(user);
    // When
    var res = buyerService.canDo(method, userID, null);
    // Then
    assertThat(res).isTrue();
  }
}
