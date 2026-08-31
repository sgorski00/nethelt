package pl.sgorski.nethelt.agent.security.storage.impl;

import java.util.Optional;
import org.springframework.stereotype.Component;
import pl.sgorski.nethelt.agent.exception.CredentialsStoreException;
import pl.sgorski.nethelt.agent.security.provider.KeyringProvider;
import pl.sgorski.nethelt.agent.security.storage.CredentialsStore;

@Component
public final class SecureCredentialsStore implements CredentialsStore {

  private static final String SERVICE_NAME = "nethelt";
  private static final String ACCOUNT_NAME = "nethelt";

  private final KeyringProvider keyringProvider;

  public SecureCredentialsStore(KeyringProvider keyringProvider) {
    this.keyringProvider = keyringProvider;
  }

  @Override
  public void save(String secret) throws CredentialsStoreException {
    try (var keyring = keyringProvider.create()) {
      keyring.setPassword(SERVICE_NAME, ACCOUNT_NAME, secret);
    } catch (Exception e) {
      throw new CredentialsStoreException("Failed to save credentials", e);
    }
  }

  @Override
  public Optional<String> get() throws CredentialsStoreException {
    try (var keyring = keyringProvider.create()) {
      var secret = keyring.getPassword(SERVICE_NAME, ACCOUNT_NAME);
      return Optional.ofNullable(secret);
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  @Override
  public void delete() throws CredentialsStoreException {
    try (var keyring = keyringProvider.create()) {
      keyring.deletePassword(SERVICE_NAME, ACCOUNT_NAME);
    } catch (Exception e) {
      throw new CredentialsStoreException("Failed to delete credentials", e);
    }
  }
}
