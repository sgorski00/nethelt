package pl.sgorski.nethelt.agent;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import picocli.CommandLine;
import picocli.spring.PicocliSpringFactory;
import pl.sgorski.nethelt.agent.command.NetheltCommand;

@SpringBootApplication(scanBasePackages = {
  "pl.sgorski.nethelt.agent.command",
  "pl.sgorski.nethelt.agent.security.storage",
  "pl.sgorski.nethelt.agent.security.provider"
})
public class CliApplication implements CommandLineRunner {

  private final ApplicationContext context;

  public CliApplication(ApplicationContext context) {
    this.context = context;
  }

  @Override
  public void run(String... args) {
    var command = context.getBean(NetheltCommand.class);
    var factory = new PicocliSpringFactory(context);
    var exitCode = new CommandLine(command, factory).execute(args);
    if (exitCode != 0) {
      System.exit(exitCode);
    }
  }

  static void main(String[] args) {
    SpringApplication.run(CliApplication.class, args);
  }
}
