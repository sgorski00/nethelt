package pl.sgorski.nethelt.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import pl.sgorski.nethelt.model.Device;
import pl.sgorski.nethelt.model.Result;

public class NetworkOperationTests {

  @Test
  void shouldCalculateElapsedTimeCorrectly() {
    NetworkOperation<Device, Result> operation = device -> null;

    long startTime = System.nanoTime();
    long elapsedTime = operation.getElapsedTimeInMs(startTime);

    assertTrue(elapsedTime >= 0);
  }
}
