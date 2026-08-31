package pl.sgorski.nethelt.webapi.utils;

import org.springframework.test.util.ReflectionTestUtils;
import pl.sgorski.nethelt.webapi.features.network.domain.Network;
import pl.sgorski.nethelt.webapi.features.user.domain.User;

public final class TestNetworkFactory {

  public static Network createNetwork(Long id) {
    var user = TestUserFactory.createLocalUser();
    var name = "Test Network";
    var description = "This is a test network";

    var network = createNetwork(user, name, description);
    ReflectionTestUtils.setField(network, "id", id);
    return network;
  }

  public static Network createNetwork() {
    var user = TestUserFactory.createLocalUser();
    var name = "Test Network";
    var description = "This is a test network";

    return createNetwork(user, name, description);
  }

  public static Network createNetwork(String name) {
    var user = TestUserFactory.createLocalUser();
    var description = "This is a test network";

    return createNetwork(user, name, description);
  }

  public static Network createNetwork(String name, String description) {
    var user = TestUserFactory.createLocalUser();

    return createNetwork(user, name, description);
  }

  public static Network createNetwork(User user, String name, String description) {
    return new Network(user, name, description);
  }
}
