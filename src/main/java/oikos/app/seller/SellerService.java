package oikos.app.seller;

import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import oikos.app.common.exceptions.EntityNotFoundException;
import oikos.app.common.models.Address;
import oikos.app.common.utils.Authorizable;
import oikos.app.users.Role;
import oikos.app.users.User;
import oikos.app.users.UserRepo;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

import static oikos.app.common.utils.AddressUtils.editAddressFromDTO;

/**
 * Created by Mohamed Haamdi on 17/04/2021.
 */
@Service @AllArgsConstructor @Slf4j public class SellerService
  implements Authorizable<SellerService.SellerMethods> {
  private final UserRepo userRepo;
  private final SellerRepo sellerRepo;
  private final ModelMapper mapper;


  @ToString enum SellerMethods {
    GET_ALL_SELLERS(Names.GET_ALL_SELLERS), UPDATE_MY_SELLER_PROFILE(
      Names.UPDATE_MY_SELLER_PROFILE), UPDATE_SELLER_PROFILE(
      Names.UPDATE_SELLER_PROFILE);
    private final String label;

    SellerMethods(String label) {
      this.label = label;
    }

    public static class Names {
      public static final String UPDATE_MY_SELLER_PROFILE =
        "UPDATE_MY_SELLER_PROFILE";
      public static final String UPDATE_SELLER_PROFILE =
        "UPDATE_SELLER_PROFILE";
      public static final String GET_ALL_SELLERS = "GET_ALL_SELLERS";

      private Names() {
      }
    }
  }

  @CacheEvict(value = "users", key = "#userID") @Transactional
  public User updateMySellerProfile(UpdateSellerRequest dto, String userID) {
    log.info("Updating seller info for the current user : {}", userID);
    var user = userRepo.getOne(userID);
    return doUpdate(dto, user);
  }

  @CacheEvict(value = "users", key = "#sellerID") @Transactional
  public User updateSellerProfile(String sellerID, UpdateSellerRequest dto) {
    log.info("Updating seller info for the user : {}", sellerID);
    var user = userRepo.findById(sellerID)
      .orElseThrow(() -> new EntityNotFoundException(User.class, sellerID));
    return doUpdate(dto, user);
  }

  @Override public boolean canDo(SellerMethods methodName, String userID,
    String objectID) {
    return switch (methodName) {
      case UPDATE_SELLER_PROFILE, GET_ALL_SELLERS -> CollectionUtils
        .containsAny(userRepo.getOne(userID).getRoles(),
          List.of(Role.SECRETARY, Role.ADMIN));
      case UPDATE_MY_SELLER_PROFILE -> true;
    };
  }

  private User doUpdate(UpdateSellerRequest dto, User user) {
    if (user.getSellerProfile() == null) {
      // We make sure that the user has the seller role.
      user.getRoles().add(Role.SELLER);
      user = userRepo.save(user);
    }
    var seller = sellerRepo.getByUserID(user.getId()).orElse(new Seller());
    mapper.map(dto, seller);
    if(seller.getAddress() == null) {seller.setAddress(new Address());}
    editAddressFromDTO(seller.getAddress(),dto.getAddress());
    seller.setUser(user);
    user.setSellerProfile(seller);
    return userRepo.save(user);
  }
}
