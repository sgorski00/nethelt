package pl.sgorski.nethelt.agent.security.storage;

import java.util.Optional;

public interface CredentialsStore {
  void save(String secret);

  Optional<String> get();

  void delete();
}
