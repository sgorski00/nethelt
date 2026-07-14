package pl.sgorski.nethelt.webapi.features.auth.oauth2.addon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.sgorski.nethelt.webapi.exception.oauth2.InvalidOAuth2UserInfoException;
import pl.sgorski.nethelt.webapi.http.client.github.GithubClient;
import pl.sgorski.nethelt.webapi.http.client.github.dto.GithubEmailEntry;

@ExtendWith(MockitoExtension.class)
public class OAuth2GithubEmailServiceTests {

  @Mock private GithubClient githubClient;
  @InjectMocks private OAuth2GithubEmailService githubEmailService;

  @Test
  void getGithubAccountEmail_shouldReturnPrimaryEmail_whenTwoArePresent() {
    var emailEntries =
        List.of(
            new GithubEmailEntry("john.doe@example.com", false),
            new GithubEmailEntry("primary@example.com", true));
    when(githubClient.fetchEmails("Bearer token")).thenReturn(emailEntries);

    var result = githubEmailService.getGithubAccountEmail("token");

    assertEquals("primary@example.com", result);
  }

  @Test
  void getGithubAccountEmail_shouldReturnPrimaryEmail_whenOnlyOneIsPresent() {
    var emailEntries = List.of(new GithubEmailEntry("primary@example.com", true));
    when(githubClient.fetchEmails("Bearer token")).thenReturn(emailEntries);

    var result = githubEmailService.getGithubAccountEmail("token");

    assertEquals("primary@example.com", result);
  }

  @Test
  void getGithubAccountEmail_shouldThrow_whenOnlyNotPrimaryIsPresent() {
    var emailEntries = List.of(new GithubEmailEntry("not-primary@example.com", false));
    when(githubClient.fetchEmails("Bearer token")).thenReturn(emailEntries);

    assertThrows(
        InvalidOAuth2UserInfoException.class,
        () -> githubEmailService.getGithubAccountEmail("token"));
  }

  @Test
  void getGithubAccountEmail_shouldThrow_whenListIsEmpty() {
    when(githubClient.fetchEmails("Bearer token")).thenReturn(List.of());

    assertThrows(
        InvalidOAuth2UserInfoException.class,
        () -> githubEmailService.getGithubAccountEmail("token"));
  }
}
