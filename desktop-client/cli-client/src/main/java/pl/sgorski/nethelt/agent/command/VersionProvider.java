package pl.sgorski.nethelt.agent.command;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import picocli.CommandLine.IVersionProvider;

@Component
public final class VersionProvider implements IVersionProvider {

  private final String version;

  public VersionProvider(@Value("${nethelt.version}") String version) {
    this.version = version;
  }

  @Override
  public String[] getVersion() {
    return new String[] {"Nethelt CLI " + version};
  }
}
