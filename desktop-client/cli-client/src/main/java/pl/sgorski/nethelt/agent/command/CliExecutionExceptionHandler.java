package pl.sgorski.nethelt.agent.command;

import picocli.CommandLine;
import picocli.CommandLine.IExecutionExceptionHandler;
import picocli.CommandLine.ParseResult;
import pl.sgorski.nethelt.agent.exception.CredentialsAccessDeniedException;

public final class CliExecutionExceptionHandler implements IExecutionExceptionHandler {

  static final String ACCESS_DENIED_MESSAGE =
      "Permission denied. Run this command as administrator (Windows) or with sudo (Linux/macOS).";

  @Override
  public int handleExecutionException(
      Exception ex, CommandLine commandLine, ParseResult parseResult) throws Exception {
    if (ex instanceof CredentialsAccessDeniedException) {
      commandLine.getErr().println(commandLine.getColorScheme().errorText(ACCESS_DENIED_MESSAGE));
      return commandLine.getCommandSpec().exitCodeOnExecutionException();
    }
    throw ex;
  }
}
