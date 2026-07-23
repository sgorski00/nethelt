package pl.sgorski.nethelt.webapi.features.network.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.sgorski.nethelt.webapi.exception.domain.network.NetworkNotFoundException;
import pl.sgorski.nethelt.webapi.exception.domain.network.NetworkValidationFailedException;
import pl.sgorski.nethelt.webapi.features.network.dto.command.NetworkCreateCommand;
import pl.sgorski.nethelt.webapi.features.network.dto.command.NetworkUpdateCommand;
import pl.sgorski.nethelt.webapi.features.network.repository.NetworkRepository;
import pl.sgorski.nethelt.webapi.features.user.service.UserService;
import pl.sgorski.nethelt.webapi.utils.TestNetworkFactory;
import pl.sgorski.nethelt.webapi.utils.TestUserFactory;

@ExtendWith(MockitoExtension.class)
public class NetworkServiceTests {

  @Mock private NetworkRepository networkRepository;
  @Mock private UserService userService;
  @InjectMocks private NetworkService networkService;

  @Test
  void getAllNetworksForUser_shouldReturnNetworks_whenNetworksExists() {
    var network1 = TestNetworkFactory.createNetwork("Network 1");
    var network2 = TestNetworkFactory.createNetwork("Network 2");
    when(networkRepository.findAllByUserIdAndDeletedAtIsNull(1L))
        .thenReturn(List.of(network1, network2));

    var result = networkService.getAllNetworksForUser(1L);

    assertEquals(2, result.size());
    assertTrue(result.contains(network1));
    assertTrue(result.contains(network2));
  }

  @Test
  void getAllNetworksForUser_shouldReturnEmptyList_whenNetworksDoesNotExists() {
    when(networkRepository.findAllByUserIdAndDeletedAtIsNull(1L)).thenReturn(List.of());

    var result = networkService.getAllNetworksForUser(1L);

    assertTrue(result.isEmpty());
  }

  @Test
  void getNetwork_shouldReturnNetwork_whenNetworkExists() {
    var network = TestNetworkFactory.createNetwork();
    when(networkRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(network));

    var result = networkService.getNetwork(1L);

    assertSame(network, result);
  }

  @Test
  void getNetwork_shouldThrow_whenNetworkIsNotPresent() {
    when(networkRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

    assertThrows(NetworkNotFoundException.class, () -> networkService.getNetwork(1L));
  }

  @Test
  void createNetwork_shouldThrow_whenUserHasMaxNetworks() {
    when(networkRepository.countByUserIdAndDeletedAtIsNull(1L))
        .thenReturn(Long.valueOf(NetworkService.MAX_NETWORKS_PER_USER));

    var command = new NetworkCreateCommand(1L, "Network Name", "Network Description");

    var thrown =
        assertThrows(
            NetworkValidationFailedException.class, () -> networkService.createNetwork(command));
    assertTrue(thrown.getMessage().contains("maximum number"));
  }

  @Test
  void createNetwork_shouldThrow_whenUserHasNetworkWithSameName() {
    var name = "Network Name";
    var userId = 1L;
    var network = TestNetworkFactory.createNetwork(name);
    when(networkRepository.countByUserIdAndDeletedAtIsNull(userId)).thenReturn(0L);
    when(networkRepository.findAllByUserIdAndNameAndDeletedAtIsNull(userId, name))
        .thenReturn(List.of(network));

    var command = new NetworkCreateCommand(userId, name, "Network Description");

    var thrown =
        assertThrows(
            NetworkValidationFailedException.class, () -> networkService.createNetwork(command));
    assertTrue(thrown.getMessage().contains("already exists"));
  }

  @Test
  void createNetwork_shouldCreateWithCorrectValues_whenValidationPassed() {
    var name = "Network Name";
    var userId = 1L;
    var user = TestUserFactory.createLocalUser();
    when(networkRepository.countByUserIdAndDeletedAtIsNull(userId)).thenReturn(0L);
    when(networkRepository.findAllByUserIdAndNameAndDeletedAtIsNull(userId, name))
        .thenReturn(List.of());
    when(userService.getUser(userId)).thenReturn(user);
    when(networkRepository.save(any())).then(invocation -> invocation.getArgument(0));

    var command = new NetworkCreateCommand(userId, name, "Network Description");

    var result = networkService.createNetwork(command);

    assertSame(user, result.getUser());
    assertEquals(name, result.getName());
    assertEquals("Network Description", result.getDescription());
    verify(networkRepository).save(result);
  }

  @Test
  void updateNetwork_shouldUpdateWithCorrectValues() {
    var command = new NetworkUpdateCommand("New Name", "New Description");
    var network = TestNetworkFactory.createNetwork("Old Name", "Old Description");
    when(networkRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(network));
    when(networkRepository.findAllByUserIdAndNameAndDeletedAtIsNull(
            nullable(Long.class), eq("New Name")))
        .thenReturn(List.of());

    var result = networkService.updateNetwork(1L, command);

    assertEquals("New Name", result.getName());
    assertEquals("New Description", result.getDescription());
  }

  @Test
  void updateNetwork_shouldUpdateWithCorrectValues_whenNameIsNotChanged() {
    var command = new NetworkUpdateCommand("Old Name", "New Description");
    var network = TestNetworkFactory.createNetwork("Old Name", "Old Description");
    when(networkRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(network));

    var result = networkService.updateNetwork(1L, command);

    assertEquals("Old Name", result.getName());
    assertEquals("New Description", result.getDescription());
    verify(networkRepository, never())
        .findAllByUserIdAndNameAndDeletedAtIsNull(nullable(Long.class), anyString());
  }

  @Test
  void updateNetwork_shouldThrow_whenNameIsChangedAndSameAlreadyExists() {
    var command = new NetworkUpdateCommand("New Name", "New Description");
    var network = TestNetworkFactory.createNetwork("Old Name", "Old Description");
    when(networkRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(network));
    when(networkRepository.findAllByUserIdAndNameAndDeletedAtIsNull(
            nullable(Long.class), eq("New Name")))
        .thenReturn(
            List.of(
                TestNetworkFactory
                    .createNetwork())); // nullable because userid is null before save to db

    assertThrows(
        NetworkValidationFailedException.class, () -> networkService.updateNetwork(1L, command));
  }

  @Test
  void deleteNetwork_shouldMarkNetworkAsDeleted() {
    var network = TestNetworkFactory.createNetwork();
    when(networkRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(network));

    networkService.deleteNetwork(1L);

    assertTrue(network.isDeleted());
  }
}
