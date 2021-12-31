package oikos.app.users;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Created by Mohamed Haamdi on 23/03/2021. */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class SigninResponse {
  String token;
}
