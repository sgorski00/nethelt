package pl.sgorski.nethelt.webapi.features.auth.oauth2.addon;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.sgorski.nethelt.webapi.exception.oauth2.InvalidOAuth2UserInfoException;
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
    throw new InvalidOAuth2UserInfoException("github primary email");
  }
}
