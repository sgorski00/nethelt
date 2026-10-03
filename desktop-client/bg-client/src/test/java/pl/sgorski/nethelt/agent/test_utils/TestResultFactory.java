package pl.sgorski.nethelt.agent.test_utils;

import pl.sgorski.nethelt.agent.model.monitoring_result.PingResult;
import pl.sgorski.nethelt.agent.model.monitoring_result.TelnetResult;

public class TestResultFactory {
  public static PingResult createPingResult(boolean result) {
    return new PingResult(1L, result, "Test result message", result ? 100 : -1);
  }

  public static TelnetResult createTelnetResult(boolean result) {
    return new TelnetResult(1L, result, "Test result message", result ? 100 : -1, result);
  }
}
