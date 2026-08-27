package pl.sgorski.nethelt.agent.command.auth;

import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

@Component
@Command(
    name = "auth",
    subcommands = {AuthRegisterCommand.class})
public final class AuthCommand implements Runnable {

  @Override
  public void run() {}
}
