package pl.sgorski.nethelt.agent.security.provider.impl;

import com.github.javakeyring.Keyring;
import org.springframework.stereotype.Component;
import pl.sgorski.nethelt.agent.exception.CredentialsStoreException;
import pl.sgorski.nethelt.agent.security.provider.KeyringProvider;

@Component
public final class DefaultKeyringProvider implements KeyringProvider {
  @Override
  public Keyring create() {
    try {
      return Keyring.create();
    } catch (Exception e) {
      throw new CredentialsStoreException("Failed to create Keyring instance", e);
    }
  }
}
