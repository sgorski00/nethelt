package pl.sgorski.nethelt.agent.command.auth;

import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import pl.sgorski.nethelt.agent.security.storage.CredentialsStore;

@Component
@Command(
    name = "status",
    description =
        "Shows if the agent is registered in the local storage. It does not check if the PAT is valid or not.")
public final class AuthStatusCommand implements Runnable {

  private final CredentialsStore credentialsStore;

  public AuthStatusCommand(CredentialsStore credentialsStore) {
    this.credentialsStore = credentialsStore;
  }

  @Override
  public void run() {
    var credentials = credentialsStore.get();
    System.out.println(
        "Credentials status: " + (credentials.isPresent() ? "Registered" : "Not registered"));
  }
}
