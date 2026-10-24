package pl.sgorski.nethelt.agent.security.storage.impl;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;
import pl.sgorski.nethelt.agent.exception.CredentialsAccessDeniedException;
import pl.sgorski.nethelt.agent.exception.CredentialsStoreException;
import pl.sgorski.nethelt.agent.security.provider.CredentialsPathProvider;
import pl.sgorski.nethelt.agent.security.storage.CredentialsStore;

/**
 * Stores the PAT in a machine-wide file shared by the CLI and the background service.
 *
 * <p>On POSIX systems the file gets {@code rw-r-----} and the group of its directory, so the
 * installer only has to give the directory to the service account group. On Windows inheritance is
 * removed and only SYSTEM and Administrators get access.
 */
@Component
public final class FileCredentialsStore implements CredentialsStore {

  private static final Set<PosixFilePermission> DIRECTORY_PERMISSIONS =
      PosixFilePermissions.fromString("rwxr-x---");
  private static final Set<PosixFilePermission> FILE_PERMISSIONS =
      PosixFilePermissions.fromString("rw-r-----");
  // Well-known SIDs, names of these accounts are localized: SYSTEM and BUILTIN\Administrators
  private static final List<String> WINDOWS_ACL_GRANTS = List.of("*S-1-5-18:F", "*S-1-5-32-544:F");

  private final CredentialsPathProvider pathProvider;

  public FileCredentialsStore(CredentialsPathProvider pathProvider) {
    this.pathProvider = pathProvider;
  }

  @Override
  public void save(String secret) throws CredentialsStoreException {
    var file = pathProvider.get().toAbsolutePath();
    Path tempFile = null;
    try {
      var directory = createDirectory(file.getParent());
      tempFile = createRestrictedFile(directory);
      Files.writeString(tempFile, secret);
      Files.move(tempFile, file, StandardCopyOption.ATOMIC_MOVE);
    } catch (AccessDeniedException e) {
      throw new CredentialsAccessDeniedException(file, e);
    } catch (IOException e) {
      throw new CredentialsStoreException("Failed to save credentials to " + file, e);
    } finally {
      deleteIfExists(tempFile);
    }
  }

  @Override
  public Optional<String> get() throws CredentialsStoreException {
    var file = pathProvider.get();
    try {
      var secret = Files.readString(file).strip();
      return secret.isEmpty() ? Optional.empty() : Optional.of(secret);
    } catch (NoSuchFileException e) {
      return Optional.empty();
    } catch (AccessDeniedException e) {
      throw new CredentialsAccessDeniedException(file, e);
    } catch (IOException e) {
      throw new CredentialsStoreException("Failed to read credentials from " + file, e);
    }
  }

  @Override
  public void delete() throws CredentialsStoreException {
    var file = pathProvider.get();
    try {
      Files.deleteIfExists(file);
    } catch (AccessDeniedException e) {
      throw new CredentialsAccessDeniedException(file, e);
    } catch (IOException e) {
      throw new CredentialsStoreException("Failed to delete credentials from " + file, e);
    }
  }

  private static Path createDirectory(Path directory) throws IOException {
    if (Files.isDirectory(directory)) return directory;

    if (isPosix(directory)) {
      return Files.createDirectories(
          directory, PosixFilePermissions.asFileAttribute(DIRECTORY_PERMISSIONS));
    }
    return Files.createDirectories(directory);
  }

  private static Path createRestrictedFile(Path directory) throws IOException {
    if (isPosix(directory)) {
      var file =
          Files.createTempFile(
              directory,
              ".credentials",
              ".tmp",
              PosixFilePermissions.asFileAttribute(FILE_PERMISSIONS));
      restrictPosixAccess(file, directory);
      return file;
    }
    var file = Files.createTempFile(directory, ".credentials", ".tmp");
    restrictWindowsAccess(file);
    return file;
  }

  private static void restrictPosixAccess(Path file, Path directory) throws IOException {
    Files.setPosixFilePermissions(file, FILE_PERMISSIONS);
    var view = Files.getFileAttributeView(file, PosixFileAttributeView.class);
    var directoryGroup = Files.readAttributes(directory, PosixFileAttributes.class).group();
    if (!view.readAttributes().group().equals(directoryGroup)) {
      view.setGroup(directoryGroup);
    }
  }

  private static void restrictWindowsAccess(Path file) throws IOException {
    var command =
        Stream.concat(
                Stream.of("icacls", file.toString(), "/inheritance:r", "/grant:r"),
                WINDOWS_ACL_GRANTS.stream())
            .toList();
    var process =
        new ProcessBuilder(command)
            .redirectErrorStream(true)
            .redirectOutput(ProcessBuilder.Redirect.DISCARD)
            .start();
    try {
      var exitCode = process.waitFor();
      if (exitCode != 0) {
        throw new IOException("icacls failed with exit code " + exitCode + " for " + file);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Interrupted while restricting access to " + file, e);
    }
  }

  private static boolean isPosix(Path path) {
    return path.getFileSystem().supportedFileAttributeViews().contains("posix");
  }

  private static void deleteIfExists(Path file) {
    if (file == null) return;

    try {
      Files.deleteIfExists(file);
    } catch (IOException ignored) {
    }
  }
}
