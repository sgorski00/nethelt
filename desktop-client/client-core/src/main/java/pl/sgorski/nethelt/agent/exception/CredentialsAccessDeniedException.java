package pl.sgorski.nethelt.agent.exception;

import java.nio.file.Path;

public final class CredentialsAccessDeniedException extends CredentialsStoreException {
  public CredentialsAccessDeniedException(Path file, Throwable cause) {
    super("Access denied to credentials file " + file, cause);
  }
}
