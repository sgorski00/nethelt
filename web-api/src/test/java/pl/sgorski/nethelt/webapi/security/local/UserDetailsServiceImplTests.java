package pl.sgorski.nethelt.webapi.security.local;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.sgorski.nethelt.webapi.features.user.service.UserService;
import pl.sgorski.nethelt.webapi.utils.TestUserFactory;

@ExtendWith(MockitoExtension.class)
public class UserDetailsServiceImplTests {

  @Mock private UserService userService;
  @InjectMocks private UserDetailsServiceImpl userDetailsService;

  @Test
  void loadUserByUsername_shouldLoadUserByEmail() {
    var user = TestUserFactory.createLocalUser();
    when(userService.getUser(anyString())).thenReturn(user);

    var result = userDetailsService.loadUserByUsername(user.getEmail());

    assertSame(user, result);
  }
}
