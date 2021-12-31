package oikos.app.users;

import oikos.app.common.models.Address;
import oikos.app.security.AuthProvider;

import java.time.LocalDate;
import java.util.Set;
/**
 * Created by Mohamed Haamdi on 06/01/2021.
 */
public interface UserInfo {
  interface SellerInfo {
    MaritalStatus getMaritalStatus();

    Civility getCivility();

    LocalDate getBirthDate();

    Address getAddress();
  }

  interface BuyerInfo {
    boolean isIsValidated();
  }

  interface UserProfileFileInfo {
    long getSize();

    String getFileType();

    String getFileName();

    String getOriginalName();

    String getId();
  }

  SellerInfo getSellerProfile();

  BuyerInfo getBuyerProfile();

  UserProfileFileInfo getUserProfileFile();

  Set<Role> getRoles();

  AuthProvider getProvider();

  String getPhoneNumber();

  String getLastName();

  String getFirstName();

  String getEmail();

  String getId();
}
