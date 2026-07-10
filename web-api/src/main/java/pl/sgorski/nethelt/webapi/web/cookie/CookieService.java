package pl.sgorski.nethelt.webapi.web.cookie;

import java.time.Duration;
import java.util.Optional;

public interface CookieService {

  void save(String name, String value, Duration expiration);

  Optional<String> find(String name);

  void delete(String name);
}
