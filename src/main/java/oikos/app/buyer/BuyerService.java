package oikos.app.buyer;

import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import oikos.app.common.exceptions.EntityNotFoundException;
import oikos.app.common.utils.Authorizable;
import oikos.app.notifications.NotificationService;
import oikos.app.users.Role;
import oikos.app.users.User;
import oikos.app.users.UserRepo;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;

/** Created by Mohamed Haamdi on 19/04/2021. */
@Service
@AllArgsConstructor
@Slf4j
public class BuyerService implements Authorizable<BuyerService.BuyerMethods> {
  private final UserRepo userRepo;
  private final NotificationService notificationService;
  @ToString
  enum BuyerMethods {
    VALIDATE_BUYER(Names.VALIDATE_BUYER);
    private final String label;

    BuyerMethods(String label) {
      this.label = label;
    }

    public static class Names {
      public static final String VALIDATE_BUYER = "VALIDATE_BUYER";

      private Names() {}
    }
  }
  @CacheEvict(value = "users", key = "#userid")
  @Transactional
  public User validateBuyer(String userid) {
    log.info("Setting the buyer {} as validated",userid);
    var user =
        userRepo
            .findById(userid)
            .orElseThrow(() -> new EntityNotFoundException(User.class, userid));
    Buyer buyer;
    var optBuyer = Optional.ofNullable(user.getBuyerProfile());
    if (optBuyer.isEmpty()) {
      // We make sure that the user has the buyer role.
      user.getRoles().add(Role.BUYER);
      buyer = new Buyer();
    } else {
      buyer = optBuyer.get();
    }
    if(!buyer.isValidated())
      notificationService.addNotification(userid,"Your buyer profile has been validated. "
          + "You can now send messages and offers to sellers and more.",null);
    buyer.setUser(user);
    buyer.setValidated(true);
    user.setBuyerProfile(buyer);
    return userRepo.save(user);
  }

  @Override
  public boolean canDo(BuyerMethods methodName, String userID, String objectID) {
    return switch (methodName) {
      case VALIDATE_BUYER ->
        CollectionUtils.containsAny(userRepo.getOne(userID).getRoles(),
          List.of(Role.SECRETARY, Role.ADMIN));
    };
  }
}
