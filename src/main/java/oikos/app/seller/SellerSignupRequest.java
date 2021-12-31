package oikos.app.seller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;

/**
 * Created by Mohamed Haamdi on 21/04/2021.
 */
@EqualsAndHashCode(callSuper = true) @Data @SuperBuilder @NoArgsConstructor @AllArgsConstructor
public class SellerSignupRequest
  extends SellerBySecretarySignupRequest {
  @NotNull private String password;
}
