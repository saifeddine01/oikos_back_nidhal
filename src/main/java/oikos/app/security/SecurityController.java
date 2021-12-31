package oikos.app.security;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import oikos.app.common.responses.DoneResponse;
import oikos.app.common.utils.Monitor;
import oikos.app.security.dtos.ForgotPasswordRequest;
import oikos.app.security.dtos.RefreshTokenResponse;
import oikos.app.security.dtos.ResetPasswordRequest;
import oikos.app.seller.SellerBySecretarySignupRequest;
import oikos.app.seller.SellerSignupRequest;
import oikos.app.serviceproviders.dtos.CollaboratorSignupRequest;
import oikos.app.serviceproviders.dtos.ServiceProviderSignupRequest;
import oikos.app.users.PasswordlessSignupRequest;
import oikos.app.users.SigninRequest;
import oikos.app.users.SigninResponse;
import oikos.app.users.SignupRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/** Created by Mohamed Haamdi on 08/02/2021 */
@Slf4j
@RestController
@AllArgsConstructor
@Validated
@Monitor
@RequestMapping("/security")
public class SecurityController {
  private final SecurityService securityService;

  @PostMapping("/signup/buyer")
  public DoneResponse signupBuyer(@Valid @RequestBody SignupRequest dto) {
    var res = securityService.registerNewBuyer(dto);
    return new DoneResponse("Activation Email has been sent with the code : " + res);
  }

  @PostMapping("/signup/seller")
  public DoneResponse signupSeller(@Valid @RequestBody SellerSignupRequest dto) {
    var res = securityService.registerNewSeller(dto);
    return new DoneResponse("Activation SMS has been sent with the code : " + res);
  }

  @PreAuthorize(
      "@securityService.canDo('REGISTER_NEW_SELLER_BY_SECRETARY',#user.username,#user.username)")
  @PostMapping("/signup/sellerBySecretary")
  public DoneResponse signupSellerBySecretary(
      @CurrentUser OikosUserDetails user, @Valid @RequestBody SellerBySecretarySignupRequest dto) {
    var res = securityService.registerNewSellerBySecretary(dto);
    return new DoneResponse("Password reset email has been sent with the code : " + res);
  }

  @PreAuthorize("@securityService.canDo('REGISTER_NEW_SECRETARY',#user.username,#user.username)")
  @PostMapping("/signup/secretary")
  public DoneResponse signupSecretary(
      @CurrentUser OikosUserDetails user, @Valid @RequestBody PasswordlessSignupRequest dto) {
    var res = securityService.registerNewSecretary(dto);
    return new DoneResponse("Password reset email has been sent with the code : " + res);
  }

  @PostMapping("/signup/provider")
  public DoneResponse signupServiceProvider(@Valid @RequestBody ServiceProviderSignupRequest dto) {
    var res = securityService.registerNewServiceProvider(dto);
    return new DoneResponse("Activation Email has been sent with the code : " + res);
  }

  @PreAuthorize("@securityService.canDo('REGISTER_NEW_COLLABORATOR',#user.username,#req.companyID)")
  @PostMapping("/signup/collaborator")
  public DoneResponse signupCollaborator(
      @CurrentUser OikosUserDetails user, @Valid @RequestBody CollaboratorSignupRequest req) {
    var res = securityService.registerNewCollaborator(req);
    return new DoneResponse("Password reset email has been sent with the code : " + res);
  }

  @PostMapping("/signin")
  public SigninResponse signin(@Valid @RequestBody SigninRequest dto) {
    return new SigninResponse(securityService.signIn(dto));
  }

  @GetMapping("/refreshToken")
  public RefreshTokenResponse refreshToken(@CurrentUser OikosUserDetails user) {
    return new RefreshTokenResponse(securityService.refreshToken());
  }

  @GetMapping("/revoke")
  public DoneResponse revokeTokens(@CurrentUser OikosUserDetails user) {
    securityService.revokeTokens(user.getUsername());
    return new DoneResponse("All your tokens have been revoked");
  }

  @GetMapping("/mailconfirm/{confirmationCode}")
  public DoneResponse confirmUserByEmail(@PathVariable String confirmationCode) {
    securityService.confirmUser(confirmationCode, VerificationTokenType.EMAIL);
    return new DoneResponse("Your account has been enabled. You can login now");
  }

  @GetMapping("/smsconfirm/{confirmationCode}")
  public DoneResponse confirmUserBySMS(@PathVariable String confirmationCode) {
    securityService.confirmUser(confirmationCode, VerificationTokenType.SMS);
    return new DoneResponse("Your account has been enabled. You can login now");
  }

  @GetMapping("/mailconfirm/{email}/resend")
  public DoneResponse resendUserConfirmationByEmail(@PathVariable String email) {
    securityService.resendUserConfirmationByEmail(email);
    return new DoneResponse("Activation email for  has been resent.");
  }

  @GetMapping("/smsconfirm/{phonenumber}/resend")
  public DoneResponse resendUserConfirmationBySMS(@PathVariable String phonenumber) {
    securityService.resendUserConfirmationBySMS(phonenumber);
    return new DoneResponse("Activation SMS has been resent.");
  }

  @PostMapping("/passwordforgetten")
  public DoneResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest dto) {
    securityService.forgotPassword(dto);
    return new DoneResponse("Password reset email has been sent.");
  }

  @PostMapping("/resetpassword/{token}")
  public DoneResponse resetPassword(
      @Valid @RequestBody ResetPasswordRequest dto, @PathVariable String token) {
    securityService.resetPassword(dto, token);
    return new DoneResponse("Password reset has been successful. Login now");
  }
}
