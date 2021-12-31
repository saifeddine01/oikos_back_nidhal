package oikos.app.users;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Data;
import oikos.app.buyer.BuyerInfoResponse;
import oikos.app.security.AuthProvider;
import oikos.app.seller.SellerInfoResponse;

import java.time.Instant;
import java.util.Set;

/**
 * Created by Mohamed Haamdi on 16/04/2021.
 */
@Data public class UserInfoResponse {
  private String id;
  private Set<Role> roles;
  private String email;
  private String phoneNumber;
  private AuthProvider provider;
  private String firstName;
  private String lastName;
  private Instant createdAt;
  @JsonUnwrapped private SellerInfoResponse sellerInfo;
  @JsonUnwrapped private BuyerInfoResponse buyerInfo;
  private UserProfileFileResponse userProfileFileInfo;
}
