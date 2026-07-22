package pl.sgorski.nethelt.webapi.features.network.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.sgorski.nethelt.webapi.exception.domain.network.NetworkNotFoundException;
import pl.sgorski.nethelt.webapi.exception.domain.network.NetworkValidationFailedException;
import pl.sgorski.nethelt.webapi.features.network.domain.Network;
import pl.sgorski.nethelt.webapi.features.network.dto.command.NetworkCreateCommand;
import pl.sgorski.nethelt.webapi.features.network.dto.command.NetworkUpdateCommand;
import pl.sgorski.nethelt.webapi.features.network.repository.NetworkRepository;
import pl.sgorski.nethelt.webapi.features.user.service.UserService;

@Service
@RequiredArgsConstructor
public class NetworkService {

  protected static final int MAX_NETWORKS_PER_USER = 5;

  private final NetworkRepository networkRepository;
  private final UserService userService;

  public List<Network> getAllNetworksForUser(Long userId) {
    return networkRepository.findAllByUserIdAndDeletedAtIsNull(userId);
  }

  public Network getNetwork(Long id) {
    return networkRepository
        .findByIdAndDeletedAtIsNull(id)
        .orElseThrow(NetworkNotFoundException::new);
  }

  @Transactional
  public Network createNetwork(NetworkCreateCommand command) {
    validateNetworkCountForUser(command.userId());
    validateNetworkNameForUser(command.userId(), command.name());

    var user = userService.getUser(command.userId());
    var network = new Network(user, command.name(), command.description());
    return networkRepository.save(network);
  }

  @Transactional
  public Network updateNetwork(Long id, NetworkUpdateCommand command) {
    var network = getNetwork(id);
    network.update(command.name(), command.description());
    return network;
  }

  @Transactional
  public void deleteNetwork(Long id) {
    var network = getNetwork(id);
    network.delete();
  }

  private void validateNetworkCountForUser(Long userId) {
    var networksCount = networkRepository.countByUserIdAndDeletedAtIsNull(userId);
    if (networksCount >= MAX_NETWORKS_PER_USER) {
      throw new NetworkValidationFailedException(
          "User has reached the maximum number of active networks.");
    }
  }

  private void validateNetworkNameForUser(Long userId, String name) {
    var networksWithSameName =
        networkRepository.findAllByUserIdAndNameAndDeletedAtIsNull(userId, name);
    if (!networksWithSameName.isEmpty()) {
      throw new NetworkValidationFailedException("Network with the same name already exists.");
    }
  }
}
