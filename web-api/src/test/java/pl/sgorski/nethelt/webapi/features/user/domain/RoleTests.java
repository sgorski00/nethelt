package pl.sgorski.nethelt.webapi.features.user.domain;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

public class RoleTests {

  @ParameterizedTest
  @EnumSource(Role.class)
  void getAuthority_shouldAddPrefix(Role role) {
    assertTrue(role.getAuthority().startsWith("ROLE_"));
  }
}
