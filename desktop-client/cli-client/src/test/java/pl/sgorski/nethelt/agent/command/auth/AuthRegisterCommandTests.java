package pl.sgorski.nethelt.agent.command.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.sgorski.nethelt.agent.security.storage.CredentialsStore;

@ExtendWith(MockitoExtension.class)
public class AuthRegisterCommandTests {

  private final ByteArrayOutputStream output = new ByteArrayOutputStream();
  private final PrintStream originalOut = System.out;

  @Mock private CredentialsStore credentialsStore;
  @InjectMocks private AuthRegisterCommand authRegisterCommand;

  @BeforeEach
  void setUp() {
    System.setOut(new PrintStream(output));
  }

  @AfterEach
  void tearDown() {
    System.setOut(originalOut);
  }

  @Test
  void run_shouldNotOverwritePat_whenPatIsAlreadyRegistered() {
    when(credentialsStore.get()).thenReturn(Optional.of("pat"));

    authRegisterCommand.run();

    assertTrue(output.toString().contains("already registered"));
    verify(credentialsStore, never()).save(anyString());
  }
}
