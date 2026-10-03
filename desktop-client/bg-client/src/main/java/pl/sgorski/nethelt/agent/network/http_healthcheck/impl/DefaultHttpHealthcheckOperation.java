package pl.sgorski.nethelt.agent.network.http_healthcheck.impl;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.springframework.stereotype.Component;
import pl.sgorski.nethelt.agent.exception.NetworkException;
import pl.sgorski.nethelt.agent.model.monitoring_result.HttpHealthcheckResult;
import pl.sgorski.nethelt.agent.model.monitoring_task.HttpHealthcheckTaskConfiguration;
import pl.sgorski.nethelt.agent.model.monitoring_task.MonitoringTask;
import pl.sgorski.nethelt.agent.network.http_healthcheck.HttpHealthcheckOperation;

@Slf4j
@Component
@RequiredArgsConstructor
public final class DefaultHttpHealthcheckOperation implements HttpHealthcheckOperation {

  private final OkHttpClient httpClient;

  @Override
  public HttpHealthcheckResult execute(MonitoringTask task) throws NetworkException {
    var configuration = (HttpHealthcheckTaskConfiguration) task.configuration();
    var url = buildUrl(task, configuration);
    var client = configureClient(task, configuration);
    var request = buildRequest(url);

    log.info("Http healthcheck of device with id: {}, url: {}", task.deviceId(), url);
    var startTime = System.nanoTime();
    try (var response = client.newCall(request).execute()) {
      var responseTime = Duration.ofNanos(System.nanoTime() - startTime).toMillis();
      var success = isExpectedStatus(response.code(), configuration);
      var message = buildMessage(response.code(), url, success, configuration);
      log.info("Http healthcheck for device with id: {}, result: {}", task.deviceId(), message);
      return new HttpHealthcheckResult(task.id(), success, message, responseTime, response.code());
    } catch (IOException e) {
      var responseTime = Duration.ofNanos(System.nanoTime() - startTime).toMillis();
      var message = "Request to " + url + " failed: " + e.getMessage();
      log.info("Http healthcheck for device with id: {}, result: {}", task.deviceId(), message);
      return new HttpHealthcheckResult(task.id(), false, message, responseTime, null);
    }
  }

  /** Without a configured expected status code any 2xx response is a success. */
  private boolean isExpectedStatus(int statusCode, HttpHealthcheckTaskConfiguration configuration) {
    var expectedStatusCode = configuration.expectedStatusCode();
    return expectedStatusCode != null
        ? statusCode == expectedStatusCode
        : statusCode >= 200 && statusCode < 300;
  }

  private String buildMessage(
      int statusCode,
      HttpUrl url,
      boolean success,
      HttpHealthcheckTaskConfiguration configuration) {
    var message = "HTTP " + statusCode + " from " + url;
    if (success) {
      return message;
    }
    var expectedStatusCode = configuration.expectedStatusCode();
    return message
        + " (expected "
        + (expectedStatusCode != null ? expectedStatusCode : "2xx")
        + ")";
  }

  private HttpUrl buildUrl(MonitoringTask task, HttpHealthcheckTaskConfiguration configuration) {
    try {
      return new HttpUrl.Builder()
          .scheme(configuration.scheme().getValue())
          .host(urlHost(task, configuration))
          .port(configuration.port())
          .encodedPath(configuration.path())
          .build();
    } catch (IllegalArgumentException e) {
      throw new NetworkException("Invalid healthcheck URL for device " + task.deviceId(), e);
    }
  }

  private String urlHost(MonitoringTask task, HttpHealthcheckTaskConfiguration configuration) {
    var host = configuration.host();
    return host != null ? host : task.deviceIp().getHostAddress();
  }

  private OkHttpClient configureClient(
      MonitoringTask task, HttpHealthcheckTaskConfiguration configuration) {
    var deviceIp = task.deviceIp();
    return httpClient
        .newBuilder()
        .callTimeout(configuration.timeout())
        .dns(_ -> List.of(deviceIp))
        .build();
  }

  /**
   * {@code Connection: close} keeps every check on a fresh connection, so the response time always
   * includes TCP connect and TLS handshake instead of depending on a pooled keep-alive connection.
   */
  private Request buildRequest(HttpUrl url) {
    return new Request.Builder().url(url).header("Connection", "close").get().build();
  }

  @Override
  public HttpHealthcheckResult error(MonitoringTask task) {
    return new HttpHealthcheckResult(task.id(), false, "HTTP Healthcheck failed", -1, null);
  }
}
