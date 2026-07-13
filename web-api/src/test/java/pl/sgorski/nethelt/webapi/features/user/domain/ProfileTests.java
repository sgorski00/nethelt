package pl.sgorski.nethelt.webapi.features.user.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

public class ProfileTests {

  @Test
  void constructor_shouldAssignCorrectValues() {
    var result = new Profile("username", "firstName", "lastName", LocalDate.of(1990, 1, 1), "bio");

    assertEquals("username", result.getUsername());
    assertEquals("firstName", result.getFirstName());
    assertEquals("lastName", result.getLastName());
    assertEquals(LocalDate.of(1990, 1, 1), result.getBirthDate());
    assertEquals("bio", result.getBio());
  }

  @Test
  void constructor_shouldThrow_whenUsernameIsBlank() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new Profile(" ", "firstName", "lastName", LocalDate.of(1990, 1, 1), "bio"));
  }

  @Test
  void assignUser_shouldAssign() {
    var user = new User();
    var profile = new Profile();

    profile.assignUser(user);

    assertSame(user, profile.getUser());
  }
}
