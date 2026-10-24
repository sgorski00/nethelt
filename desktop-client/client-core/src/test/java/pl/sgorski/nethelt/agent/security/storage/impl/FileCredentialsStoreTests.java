package pl.sgorski.nethelt.agent.security.storage.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import pl.sgorski.nethelt.agent.exception.CredentialsAccessDeniedException;
import pl.sgorski.nethelt.agent.exception.CredentialsStoreException;

public class FileCredentialsStoreTests {

  @TempDir private Path tempDir;
  private Path file;
  private FileCredentialsStore credentialsStore;

  @BeforeEach
  void setUp() {
    file = tempDir.resolve("nethelt").resolve("credentials");
    credentialsStore = new FileCredentialsStore(() -> file);
  }

  @Test
  void save_shouldWriteSecretToFile() throws IOException {
    credentialsStore.save("my-pat");

    assertEquals("my-pat", Files.readString(file));
  }

  @Test
  void save_shouldOverwriteExistingSecret() throws IOException {
    credentialsStore.save("old-pat");

    credentialsStore.save("new-pat");

    assertEquals("new-pat", Files.readString(file));
  }

  @Test
  void save_shouldNotLeaveTempFiles() throws IOException {
    credentialsStore.save("my-pat");

    try (var files = Files.list(file.getParent())) {
      assertEquals(1, files.count());
    }
  }

  @Test
  @EnabledOnOs({OS.LINUX, OS.MAC})
  void save_shouldRestrictPosixPermissions() throws IOException {
    credentialsStore.save("my-pat");

    assertEquals("rw-r-----", PosixFilePermissions.toString(Files.getPosixFilePermissions(file)));
    assertEquals(
        "rwxr-x---",
        PosixFilePermissions.toString(Files.getPosixFilePermissions(file.getParent())));
  }

  @Test
  @EnabledOnOs(OS.WINDOWS)
  void save_shouldRestrictWindowsAcl() throws IOException {
    credentialsStore.save("my-pat");

    var acl = Files.getFileAttributeView(file, AclFileAttributeView.class).getAcl();
    assertEquals(2, acl.size());
  }

  @Test
  @EnabledOnOs({OS.LINUX, OS.MAC})
  void save_shouldThrowAccessDenied_whenDirectoryIsNotWritable() throws IOException {
    Files.createDirectories(file.getParent());
    Files.setPosixFilePermissions(file.getParent(), PosixFilePermissions.fromString("r-x------"));
    assumeNotRoot();

    assertThrows(CredentialsAccessDeniedException.class, () -> credentialsStore.save("my-pat"));
  }

  @Test
  void get_shouldReturnSecret() throws IOException {
    Files.createDirectories(file.getParent());
    Files.writeString(file, "my-pat\n");

    var result = credentialsStore.get();

    assertEquals(Optional.of("my-pat"), result);
  }

  @Test
  void get_shouldReturnEmpty_whenFileDoesNotExist() {
    var result = credentialsStore.get();

    assertTrue(result.isEmpty());
  }

  @Test
  void get_shouldReturnEmpty_whenFileIsBlank() throws IOException {
    Files.createDirectories(file.getParent());
    Files.writeString(file, "  \n");

    var result = credentialsStore.get();

    assertTrue(result.isEmpty());
  }

  @Test
  @EnabledOnOs({OS.LINUX, OS.MAC})
  void get_shouldThrowAccessDenied_whenFileIsNotReadable() throws IOException {
    credentialsStore.save("my-pat");
    Files.setPosixFilePermissions(file, PosixFilePermissions.fromString("---------"));
    assumeNotRoot();

    assertThrows(CredentialsAccessDeniedException.class, () -> credentialsStore.get());
  }

  @Test
  void get_shouldThrowException_whenPathIsDirectory() throws IOException {
    Files.createDirectories(file);

    assertThrows(CredentialsStoreException.class, () -> credentialsStore.get());
  }

  @Test
  void delete_shouldDeleteFile() {
    credentialsStore.save("my-pat");

    credentialsStore.delete();

    assertFalse(Files.exists(file));
  }

  @Test
  void delete_shouldNotThrow_whenFileDoesNotExist() {
    assertDoesNotThrow(() -> credentialsStore.delete());
  }

  @Test
  @EnabledOnOs({OS.LINUX, OS.MAC})
  void delete_shouldThrowAccessDenied_whenDirectoryIsNotWritable() throws IOException {
    credentialsStore.save("my-pat");
    Files.setPosixFilePermissions(file.getParent(), PosixFilePermissions.fromString("r-x------"));
    assumeNotRoot();

    assertThrows(CredentialsAccessDeniedException.class, () -> credentialsStore.delete());
  }

  private static void assumeNotRoot() {
    assumeFalse("root".equals(System.getProperty("user.name")), "root ignores file permissions");
  }
}
