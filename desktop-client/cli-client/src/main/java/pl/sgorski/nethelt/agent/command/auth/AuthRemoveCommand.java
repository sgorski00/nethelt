package pl.sgorski.nethelt.agent.command.auth;

import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import pl.sgorski.nethelt.agent.security.storage.CredentialsStore;

@Component
@Command(name = "remove", description = "Removes an agent credentials from the local storage")
public final class AuthRemoveCommand implements Runnable {

  private final CredentialsStore credentialsStore;

  public AuthRemoveCommand(CredentialsStore credentialsStore) {
    this.credentialsStore = credentialsStore;
  }

  @Override
  public void run() {
    var console = System.console();
    if (console == null) {
      throw new IllegalStateException("Interactive console is not available.");
    }

    var input = console.readLine("Remove stored Personal Access Token? [y/N]: ");
    if (!input.equalsIgnoreCase("y") && !input.equalsIgnoreCase("yes")) {
      System.out.println("Operation cancelled.");
      return;
    }
    credentialsStore.delete();
    System.out.println("PAT removed successfully.");
  }
}
