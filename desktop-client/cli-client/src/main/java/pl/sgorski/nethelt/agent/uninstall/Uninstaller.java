package pl.sgorski.nethelt.agent.uninstall;

public interface Uninstaller {

  /**
   * @throws UninstallException when the current process cannot remove the installation
   */
  void verifyPrivileges();

  /**
   * @throws UninstallException when the client was not installed with the installer
   */
  void verifyInstallation();

  /**
   * @param purge also removes credentials, logs and the service account
   * @param quiet the user already confirmed, the uninstaller should not ask again
   * @return exit code of the command
   */
  int uninstall(boolean purge, boolean quiet);
}
