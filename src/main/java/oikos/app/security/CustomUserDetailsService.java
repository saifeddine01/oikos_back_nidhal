package oikos.app.security;

import lombok.RequiredArgsConstructor;
import oikos.app.common.exceptions.EntityNotFoundException;
import oikos.app.users.User;
import oikos.app.users.UserRepo;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Created by Mohamed Haamdi on 30/04/21. */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepo userRepository;

  @Override
  @Transactional
  public UserDetails loadUserByUsername(String emailOrPhone) throws UsernameNotFoundException {
    var userID =
        userRepository
            .findIDByEmailOrPhoneNumber(emailOrPhone)
            .orElseThrow(() -> new UsernameNotFoundException("User not found : " + emailOrPhone));
    var user = userRepository.getOne(userID);
    return OikosUserDetails.create(user);
  }

  @Transactional
  public UserDetails loadUserById(String id) {
    if (!userRepository.existsById(id)) {
      throw new EntityNotFoundException(User.class, id);
    }
    return OikosUserDetails.create(userRepository.getOne(id));
  }
}
