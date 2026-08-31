package pl.sgorski.nethelt.agent.command.auth;

import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

@Component
@Command(
    name = "auth",
    description = "Authentication commands",
    subcommands = {AuthRegisterCommand.class, AuthRemoveCommand.class, AuthStatusCommand.class})
public final class AuthCommand implements Runnable {

  @Override
  public void run() {}
}
