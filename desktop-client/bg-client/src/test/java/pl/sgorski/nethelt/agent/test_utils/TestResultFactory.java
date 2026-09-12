package pl.sgorski.nethelt.agent.test_utils;

import pl.sgorski.nethelt.agent.model.PingResult;
import pl.sgorski.nethelt.agent.model.TelnetResult;

public class TestResultFactory {
  public static PingResult createPingResult(boolean result) {
    var device = TestDeviceFactory.createDeviceWithoutPort();
    return new PingResult(device, result, "Test result message", result ? 100 : -1);
  }

  public static TelnetResult createTelnetResult(boolean result) {
    var device = TestDeviceFactory.createDeviceWithoutPort();
    return new TelnetResult(device, result, "Test result message", result ? 100 : -1, result);
  }
}
