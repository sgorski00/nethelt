package pl.sgorski.nethelt.webapi.http.client.github;

import java.util.List;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.GetExchange;
import pl.sgorski.nethelt.webapi.http.client.github.dto.GithubEmailEntry;

public interface GithubClient {
  @GetExchange("/user/emails")
  List<GithubEmailEntry> fetchEmails(@RequestHeader("Authorization") String authHeader);
}
