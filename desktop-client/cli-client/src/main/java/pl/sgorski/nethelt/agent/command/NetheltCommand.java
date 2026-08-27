package pl.sgorski.nethelt.agent.command;

import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import pl.sgorski.nethelt.agent.command.auth.AuthCommand;

@Component
@Command(
    name = "nethelt",
    mixinStandardHelpOptions = true,
    subcommands = {AuthCommand.class})
public final class NetheltCommand implements Runnable {
  @Override
  public void run() {
    System.out.println("Nethelt CLI Client. Use --help for more information.");
  }
}
