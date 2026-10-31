package pl.sgorski.nethelt.agent.command.uninstall;

import java.util.concurrent.Callable;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Spec;
import pl.sgorski.nethelt.agent.uninstall.Uninstaller;

@Component
@Command(
    name = "uninstall",
    description = "Removes the NetHelt client: the CLI and the background service")
public final class UninstallCommand implements Callable<Integer> {

  private final Uninstaller uninstaller;

  @Spec private CommandSpec spec;

  @Option(
      names = "--purge",
      description = "Also remove the credentials, logs and the service account")
  private boolean purge;

  @Option(
      names = {"-y", "--yes"},
      description = "Do not ask for confirmation")
  private boolean yes;

  public UninstallCommand(Uninstaller uninstaller) {
    this.uninstaller = uninstaller;
  }

  @Override
  public Integer call() {
    uninstaller.verifyPrivileges();
    uninstaller.verifyInstallation();

    if (!yes && !confirm()) {
      System.out.println("Operation cancelled.");
      return 0;
    }
    return uninstaller.uninstall(purge, yes);
  }

  private boolean confirm() {
    var console = System.console();
    if (console == null) {
      throw new ParameterException(
          spec.commandLine(), "Interactive console is not available. Use --yes to confirm.");
    }
    var question =
        purge
            ? "Remove NetHelt together with its credentials, logs and service account? [y/N]: "
            : "Remove NetHelt (the CLI and the background service)? [y/N]: ";
    var input = console.readLine(question);
    return input != null && (input.equalsIgnoreCase("y") || input.equalsIgnoreCase("yes"));
  }
}
