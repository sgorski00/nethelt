package pl.sgorski.nethelt.service;

import pl.sgorski.nethelt.exception.NetworkException;
import pl.sgorski.nethelt.model.Device;
import pl.sgorski.nethelt.model.Result;

/**
 * A generic interface for performing network operations on devices.
 *
 * @param <I> The type of the input device, must extend {@link Device}.
 * @param <O> The type of the output result, must extend {@link Result}.
 */
public interface NetworkOperation<I extends Device, O extends Result> {
  /**
   * Executes a network operation on the given device.
   *
   * @param device The device on which the operation is to be performed.
   * @return The result of the network operation.
   * @throws NetworkException If there is an error during the network operation.
   */
  O execute(I device) throws NetworkException;

  /**
   * Utility method to calculate elapsed time in milliseconds.
   * Should be used after {@link #execute(Device)} method.
   *
   * @param startTimeNs The start time in nanoseconds. Typically obtained from {@link System#nanoTime()} before the execution starts.
   * @return The elapsed time in milliseconds.
   */
  default long getElapsedTimeInMs(long startTimeNs) {
    return (System.nanoTime() - startTimeNs) / 1_000_000;
  }
}
