package pl.sgorski.nethelt.webapi.features.network.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;
import pl.sgorski.nethelt.webapi.features.network.dto.request.NetworkCreateRequest;
import pl.sgorski.nethelt.webapi.features.network.dto.request.NetworkUpdateRequest;
import pl.sgorski.nethelt.webapi.features.network.dto.response.NetworkResponse;
import pl.sgorski.nethelt.webapi.features.network.mapper.NetworkMapper;
import pl.sgorski.nethelt.webapi.features.network.service.NetworkService;
import pl.sgorski.nethelt.webapi.security.authenticated.AuthenticatedUserResolver;

@RestController
@RequestMapping(value = "/networks", version = "1")
@RequiredArgsConstructor
public class NetworkController {

  private final AuthenticatedUserResolver authenticatedUserResolver;
  private final NetworkService networkService;
  private final NetworkMapper networkMapper;

  @GetMapping
  public ResponseEntity<List<NetworkResponse>> getUserNetworks(Authentication authentication) {
    var userId = authenticatedUserResolver.requireUserId(authentication);
    var networks =
        networkService.getAllNetworksForUser(userId).stream()
            .map(networkMapper::toResponse)
            .toList();
    return ResponseEntity.ok(networks);
  }

  @GetMapping("/{id}")
  @PreAuthorize("@networkAuthorization.isOwner(authentication, #networkId)")
  public ResponseEntity<NetworkResponse> getNetwork(
      @P("networkId") @PathVariable("id") Long networkId, Authentication authentication) {
    var network = networkService.getNetwork(networkId);
    return ResponseEntity.ok(networkMapper.toResponse(network));
  }

  @PostMapping
  public ResponseEntity<NetworkResponse> createNetwork(
      @RequestBody @Valid NetworkCreateRequest request, Authentication authentication) {
    var userId = authenticatedUserResolver.requireUserId(authentication);
    var command = networkMapper.toCommand(request, userId);
    var network = networkService.createNetwork(command);
    return ResponseEntity.status(201).body(networkMapper.toResponse(network));
  }

  @PutMapping("/{id}")
  @PreAuthorize("@networkAuthorization.isOwner(authentication, #networkId)")
  public ResponseEntity<NetworkResponse> updateNetwork(
      @PathVariable("id") Long networkId,
      @RequestBody @Valid NetworkUpdateRequest request,
      Authentication authentication) {
    var command = networkMapper.toCommand(request);
    var network = networkService.updateNetwork(networkId, command);
    return ResponseEntity.ok(networkMapper.toResponse(network));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("@networkAuthorization.isOwner(authentication, #networkId)")
  public ResponseEntity<Void> deleteNetwork(
      @PathVariable("id") Long networkId, Authentication authentication) {
    networkService.deleteNetwork(networkId);
    return ResponseEntity.noContent().build();
  }
}
