package pl.sgorski.nethelt.webapi.features.auth.oauth2.login;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.sgorski.nethelt.webapi.http.client.github.GithubClient;

@Service
@RequiredArgsConstructor
public class OAuth2GithubEmailService {

  private final GithubClient githubClient;

  public String getGithubAccountEmail(String accessToken) {
    var response = githubClient.fetchEmails("Bearer " + accessToken);
    for (var emailEntry : response) {
      if (emailEntry.isPrimary()) {
        return emailEntry.email();
      }
    }
    throw new IllegalStateException(
        "Github account does not have pinned isPrimary email address. Could not create or link an account.");
  }
}
