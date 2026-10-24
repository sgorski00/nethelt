package pl.sgorski.nethelt.agent.command;

import static org.junit.jupiter.api.Assertions.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.AccessDeniedException;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import pl.sgorski.nethelt.agent.exception.CredentialsAccessDeniedException;

public class CliExecutionExceptionHandlerTests {

  private final StringWriter errors = new StringWriter();
  private RuntimeException toThrow;
  private CommandLine commandLine;

  @BeforeEach
  void setUp() {
    commandLine =
        new CommandLine(new FailingCommand())
            .setExecutionExceptionHandler(new CliExecutionExceptionHandler());
    commandLine.setErr(new PrintWriter(errors));
  }

  @Test
  void handle_shouldPrintHint_whenCredentialsAccessIsDenied() {
    var file = Path.of("/etc/nethelt/credentials");
    toThrow =
        new CredentialsAccessDeniedException(file, new AccessDeniedException(file.toString()));

    var exitCode = commandLine.execute();

    assertEquals(1, exitCode);
    assertTrue(errors.toString().contains(CliExecutionExceptionHandler.ACCESS_DENIED_MESSAGE));
  }

  @Test
  void handle_shouldRethrow_whenExceptionIsNotHandled() {
    toThrow = new IllegalStateException("boom");

    var exitCode = commandLine.execute();

    assertEquals(1, exitCode);
    assertFalse(errors.toString().contains(CliExecutionExceptionHandler.ACCESS_DENIED_MESSAGE));
    assertTrue(errors.toString().contains("boom"));
  }

  @Command(name = "failing")
  private final class FailingCommand implements Runnable {
    @Override
    public void run() {
      throw toThrow;
    }
  }
}
