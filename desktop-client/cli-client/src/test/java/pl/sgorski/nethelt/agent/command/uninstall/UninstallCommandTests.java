package pl.sgorski.nethelt.agent.command.uninstall;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import picocli.CommandLine;
import pl.sgorski.nethelt.agent.command.CliExecutionExceptionHandler;
import pl.sgorski.nethelt.agent.uninstall.UninstallException;
import pl.sgorski.nethelt.agent.uninstall.Uninstaller;

@ExtendWith(MockitoExtension.class)
public class UninstallCommandTests {

  private final StringWriter errors = new StringWriter();

  @Mock private Uninstaller uninstaller;
  private CommandLine commandLine;

  @BeforeEach
  void setUp() {
    commandLine =
        new CommandLine(new UninstallCommand(uninstaller))
            .setExecutionExceptionHandler(new CliExecutionExceptionHandler());
    commandLine.setErr(new PrintWriter(errors, true));
  }

  @Test
  void call_shouldUninstall_whenConfirmedWithYes() {
    when(uninstaller.uninstall(false, true)).thenReturn(0);

    var exitCode = commandLine.execute("--yes");

    assertEquals(0, exitCode);
    verify(uninstaller).verifyPrivileges();
    verify(uninstaller).verifyInstallation();
    verify(uninstaller).uninstall(false, true);
  }

  @Test
  void call_shouldPassPurge_whenPurgeIsSet() {
    when(uninstaller.uninstall(true, true)).thenReturn(0);

    var exitCode = commandLine.execute("-y", "--purge");

    assertEquals(0, exitCode);
    verify(uninstaller).uninstall(true, true);
  }

  @Test
  void call_shouldReturnExitCodeOfUninstaller() {
    when(uninstaller.uninstall(false, true)).thenReturn(3);

    assertEquals(3, commandLine.execute("--yes"));
  }

  @Test
  void call_shouldPrintMessage_whenPrivilegesAreMissing() {
    doThrow(new UninstallException("Run it with sudo")).when(uninstaller).verifyPrivileges();

    var exitCode = commandLine.execute("--yes");

    assertEquals(1, exitCode);
    assertEquals("Run it with sudo", errors.toString().strip());
    verify(uninstaller, never()).verifyInstallation();
    verify(uninstaller, never()).uninstall(anyBoolean(), anyBoolean());
  }

  @Test
  void call_shouldNotUninstall_whenNotInstalled() {
    doThrow(new UninstallException("Uninstaller was not found"))
        .when(uninstaller)
        .verifyInstallation();

    var exitCode = commandLine.execute("--yes");

    assertEquals(1, exitCode);
    assertEquals("Uninstaller was not found", errors.toString().strip());
    verify(uninstaller, never()).uninstall(anyBoolean(), anyBoolean());
  }
}
