package pl.sgorski.nethelt.agent.security.storage.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import com.github.javakeyring.Keyring;
import com.github.javakeyring.PasswordAccessException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.sgorski.nethelt.agent.exception.CredentialsStoreException;
import pl.sgorski.nethelt.agent.security.provider.KeyringProvider;

@ExtendWith(MockitoExtension.class)
public class SecureCredentialsStoreTests {

  @Mock private KeyringProvider keyringProvider;
  @Mock private Keyring keyring;
  @InjectMocks private SecureCredentialsStore credentialsStore;

  @BeforeEach
  void setUp() {
    when(keyringProvider.create()).thenReturn(keyring);
  }

  @Test
  void save_shouldSaveSecret() throws PasswordAccessException {
    credentialsStore.save("my-pat");

    verify(keyring).setPassword("nethelt", "nethelt", "my-pat");
  }

  @Test
  void get_shouldReturnSecret() throws PasswordAccessException {
    when(keyring.getPassword("nethelt", "nethelt")).thenReturn("my-pat");

    var result = credentialsStore.get();

    assertTrue(result.isPresent());
    Assertions.assertEquals("my-pat", result.get());
  }

  @Test
  void get_shouldReturnEmpty_whenSecretDoesNotExist() throws PasswordAccessException {
    when(keyring.getPassword("nethelt", "nethelt")).thenReturn(null);

    var result = credentialsStore.get();

    assertTrue(result.isEmpty());
  }

  @Test
  void delete_shouldDeleteSecret() throws PasswordAccessException {
    credentialsStore.delete();

    verify(keyring).deletePassword("nethelt", "nethelt");
  }

  @Test
  void save_shouldThrowException_whenSavingFails() throws PasswordAccessException {
    doThrow(new RuntimeException("Keyring error"))
        .when(keyring)
        .setPassword("nethelt", "nethelt", "my-pat");

    assertThrows(CredentialsStoreException.class, () -> credentialsStore.save("my-pat"));
  }

  @Test
  void get_shouldThrowException_whenRetrievingFails() throws PasswordAccessException {
    doThrow(new RuntimeException("Keyring error")).when(keyring).getPassword("nethelt", "nethelt");

    var result = credentialsStore.get();

    assertTrue(result.isEmpty());
  }

  @Test
  void delete_shouldThrowException_whenDeletingFails() throws PasswordAccessException {
    doThrow(new RuntimeException("Keyring error"))
        .when(keyring)
        .deletePassword("nethelt", "nethelt");

    assertThrows(CredentialsStoreException.class, () -> credentialsStore.delete());
  }
}
