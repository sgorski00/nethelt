package pl.sgorski.nethelt.agent.security.provider;

import com.github.javakeyring.Keyring;

public interface KeyringProvider {
  Keyring create();
}
