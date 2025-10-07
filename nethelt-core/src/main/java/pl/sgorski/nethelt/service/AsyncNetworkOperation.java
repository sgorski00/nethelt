package pl.sgorski.nethelt.service;

import java.util.concurrent.CompletableFuture;
import pl.sgorski.nethelt.exception.NetworkException;
import pl.sgorski.nethelt.model.Device;
import pl.sgorski.nethelt.model.Result;

/**
 * A generic interface for performing asynchronous operations on devices.
 *
 * @param <I> The type of the input device, must extend {@link Device}.
 * @param <O> The type of the output result, must extend {@link Result}.
 */
public interface AsyncNetworkOperation<I extends Device, O extends Result> extends NetworkOperation<I, O> {

  /**
   * Executes an asynchronous operation on the given device.
   *
   * @param device The device on which the operation is to be performed.
   * @return A {@link CompletableFuture} representing the result of the asynchronous operation.
   * @throws NetworkException If there is an error during the operation.
   */
  CompletableFuture<O> executeAsync(I device) throws NetworkException;
}
