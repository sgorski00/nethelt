package pl.sgorski.nethelt.agent.security.provider.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import com.github.javakeyring.Keyring;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import pl.sgorski.nethelt.agent.exception.CredentialsStoreException;
import pl.sgorski.nethelt.agent.security.provider.KeyringProvider;

public class DefaultKeyringProviderTests {

  private KeyringProvider keyringProvider;

  @BeforeEach
  void setUp() {
    keyringProvider = new DefaultKeyringProvider();
  }

  @Test
  void create_shouldCreateKeyring() {
    var keyring = mock(Keyring.class);

    try (MockedStatic<Keyring> mockedKeyring = mockStatic(Keyring.class)) {
      mockedKeyring.when(Keyring::create).thenReturn(keyring);

      var result = keyringProvider.create();

      assertNotNull(result);
      assertSame(keyring, result);
    }
  }

  @Test
  void create_shouldThrowException_whenKeyringCreationFails() {
    try (MockedStatic<Keyring> mockedKeyring = mockStatic(Keyring.class)) {
      mockedKeyring.when(Keyring::create).thenThrow(new RuntimeException("Keyring unavailable"));

      assertThrows(CredentialsStoreException.class, () -> keyringProvider.create());
    }
  }
}
