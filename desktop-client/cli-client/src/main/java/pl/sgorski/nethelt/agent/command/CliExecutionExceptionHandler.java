package pl.sgorski.nethelt.agent.command;

import picocli.CommandLine;
import picocli.CommandLine.IExecutionExceptionHandler;
import picocli.CommandLine.ParseResult;
import pl.sgorski.nethelt.agent.exception.CredentialsAccessDeniedException;
import pl.sgorski.nethelt.agent.uninstall.UninstallException;

public final class CliExecutionExceptionHandler implements IExecutionExceptionHandler {

  static final String ACCESS_DENIED_MESSAGE =
      "Permission denied. Run this command as administrator (Windows) or with sudo (Linux/macOS).";

  @Override
  public int handleExecutionException(
      Exception ex, CommandLine commandLine, ParseResult parseResult) throws Exception {
    if (ex instanceof CredentialsAccessDeniedException) {
      return printErrorAndGetExitCode(commandLine, ACCESS_DENIED_MESSAGE);
    }
    if (ex instanceof UninstallException) {
      return printErrorAndGetExitCode(commandLine, ex.getMessage());
    }
    throw ex;
  }

  private int printErrorAndGetExitCode(CommandLine commandLine, String message) {
    commandLine.getErr().println(commandLine.getColorScheme().errorText(message));
    return commandLine.getCommandSpec().exitCodeOnExecutionException();
  }
}
