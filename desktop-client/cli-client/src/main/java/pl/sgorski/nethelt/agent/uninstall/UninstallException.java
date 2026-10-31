package pl.sgorski.nethelt.agent.uninstall;

public class UninstallException extends RuntimeException {

  public UninstallException(String message) {
    super(message);
  }

  public UninstallException(String message, Throwable cause) {
    super(message, cause);
  }
}
