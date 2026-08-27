package pl.sgorski.nethelt.agent.command.auth;

import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import pl.sgorski.nethelt.agent.security.storage.CredentialsStore;

@Component
@Command(
    name = "register",
    description = "Register an agent with a PAT retrieved from the web client")
public final class AuthRegisterCommand implements Runnable {

  private final CredentialsStore credentialsStore;

  public AuthRegisterCommand(CredentialsStore credentialsStore) {
    this.credentialsStore = credentialsStore;
  }

  @Override
  public void run() {
    var console = System.console();
    if (console == null) {
      throw new IllegalStateException("Interactive console is not available.");
    }

    var pat = console.readPassword("Enter Personal Access Token: ");
    credentialsStore.save(new String(pat));
    System.out.println("PAT registered successfully.");
  }
}
