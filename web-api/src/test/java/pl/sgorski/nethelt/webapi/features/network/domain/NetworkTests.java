package pl.sgorski.nethelt.webapi.features.network.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import pl.sgorski.nethelt.webapi.utils.TestNetworkFactory;
import pl.sgorski.nethelt.webapi.utils.TestUserFactory;

public class NetworkTests {

  @Test
  void constructor_shouldCreateNetworkWithGivenParameters() {
    var user = TestUserFactory.createLocalUser();
    var name = "Test Network";
    var description = "This is a test network";

    var network = new Network(user, name, description);

    assertEquals(user, network.getUser());
    assertEquals(name, network.getName());
    assertEquals(description, network.getDescription());
  }

  @Test
  void delete_shouldSetDeletedAtToCurrentTime() {
    var network = TestNetworkFactory.createNetwork();

    assertFalse(network.isDeleted());
    network.delete();
    assertTrue(network.isDeleted());
  }

  @Test
  void isDeleted_shouldReturnTrue_whenDeletedAtIsNotNull() {
    var network = TestNetworkFactory.createNetwork();

    network.delete();

    assertTrue(network.isDeleted());
  }

  @Test
  void isDeleted_shouldReturnFalse_whenDeletedAtIsNull() {
    var network = TestNetworkFactory.createNetwork();

    assertFalse(network.isDeleted());
  }
}
