package pl.sgorski.nethelt.agent.command.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import picocli.CommandLine;
import pl.sgorski.nethelt.agent.security.storage.CredentialsStore;

@ExtendWith(MockitoExtension.class)
public class AuthRegisterCommandTests {

  private final ByteArrayOutputStream output = new ByteArrayOutputStream();
  private final StringWriter errors = new StringWriter();
  private final PrintStream originalOut = System.out;
  private final InputStream originalIn = System.in;

  @Mock private CredentialsStore credentialsStore;
  private CommandLine commandLine;

  @BeforeEach
  void setUp() {
    System.setOut(new PrintStream(output));
    commandLine = new CommandLine(new AuthRegisterCommand(credentialsStore));
    commandLine.setErr(new PrintWriter(errors));
  }

  @AfterEach
  void tearDown() {
    System.setOut(originalOut);
    System.setIn(originalIn);
  }

  @Test
  void run_shouldNotOverwritePat_whenPatIsAlreadyRegistered() {
    when(credentialsStore.get()).thenReturn(Optional.of("pat"));

    var exitCode = commandLine.execute();

    assertEquals(0, exitCode);
    assertTrue(output.toString().contains("already registered"));
    verify(credentialsStore, never()).save(anyString());
  }

  @Test
  void run_shouldSavePatFromStdin_whenStdinOptionIsUsed() {
    when(credentialsStore.get()).thenReturn(Optional.empty());
    setStdin("  my-pat  \nignored\n");

    var exitCode = commandLine.execute("--stdin");

    assertEquals(0, exitCode);
    verify(credentialsStore).save("my-pat");
    assertTrue(output.toString().contains("registered successfully"));
  }

  @Test
  void run_shouldFail_whenPatFromStdinIsEmpty() {
    when(credentialsStore.get()).thenReturn(Optional.empty());
    setStdin("");

    var exitCode = commandLine.execute("--stdin");

    assertNotEquals(0, exitCode);
    assertTrue(errors.toString().contains("cannot be empty"));
    verify(credentialsStore, never()).save(anyString());
  }

  @Test
  void run_shouldFail_whenConsoleIsNotAvailable() {
    assumeNoConsole();
    when(credentialsStore.get()).thenReturn(Optional.empty());

    var exitCode = commandLine.execute();

    assertNotEquals(0, exitCode);
    assertTrue(errors.toString().contains("Use --stdin"));
    verify(credentialsStore, never()).save(anyString());
  }

  private static void setStdin(String content) {
    System.setIn(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
  }

  private static void assumeNoConsole() {
    assumeTrue(
        System.console() == null || !System.console().isTerminal(),
        "test needs a non-interactive console");
  }
}
