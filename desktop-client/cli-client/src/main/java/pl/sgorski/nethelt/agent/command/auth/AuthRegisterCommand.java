package pl.sgorski.nethelt.agent.command.auth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Spec;
import pl.sgorski.nethelt.agent.security.storage.CredentialsStore;

@Component
@Command(
    name = "register",
    description = "Register an agent with a PAT retrieved from the web client")
public final class AuthRegisterCommand implements Runnable {

  private final CredentialsStore credentialsStore;

  @Spec private CommandSpec spec;

  @Option(
      names = "--stdin",
      description = "Read the PAT from standard input instead of prompting for it")
  private boolean stdin;

  public AuthRegisterCommand(CredentialsStore credentialsStore) {
    this.credentialsStore = credentialsStore;
  }

  @Override
  public void run() {
    if (credentialsStore.get().isPresent()) {
      System.out.println(
          "A Personal Access Token is already registered. Use 'remove' command to delete it first.");
      return;
    }

    var pat = (stdin ? readFromStdin() : readFromConsole()).strip();
    if (pat.isEmpty()) {
      throw new ParameterException(spec.commandLine(), "Personal Access Token cannot be empty.");
    }

    credentialsStore.save(pat);
    System.out.println("PAT registered successfully.");
  }

  private String readFromConsole() {
    var console = System.console();
    if (console == null || !console.isTerminal()) {
      throw new ParameterException(
          spec.commandLine(), "Interactive console is not available. Use --stdin instead.");
    }
    var pat = console.readPassword("Enter Personal Access Token: ");
    return pat == null ? "" : new String(pat);
  }

  private static String readFromStdin() {
    var reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
    try {
      var line = reader.readLine();
      return line == null ? "" : line;
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to read PAT from standard input", e);
    }
  }
}
