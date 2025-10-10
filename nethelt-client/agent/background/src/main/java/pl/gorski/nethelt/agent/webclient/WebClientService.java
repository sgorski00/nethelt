package pl.gorski.nethelt.agent.webclient;

import java.io.IOException;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.gorski.nethelt.agent.config.WebClientSingleton;
import pl.sgorski.nethelt.agent.serialization.SerializationController;
import pl.sgorski.nethelt.exception.WebClientException;
import pl.sgorski.nethelt.model.Device;
import pl.sgorski.nethelt.model.NetworkConfig;
import pl.sgorski.nethelt.model.Result;
import pl.sgorski.nethelt.utils.CollectionUtils;

//TODO: add unit tests

/**
 * Service for communicating with the web server.
 */
public class WebClientService {

  private static final String BASE_URL = "http://localhost:8080/api";
  private static final String DEVICES_ENDPOINT = BASE_URL + "/devices";
  private static final String PING_ENDPOINT = BASE_URL + "/ping";
  private static final String TELNET_ENDPOINT = BASE_URL + "/telnet";
  private static final String JSON_MEDIA_TYPE = "application/json";
  private static final Logger LOG = LoggerFactory.getLogger(WebClientService.class);

  private final OkHttpClient webServerClient;
  private final SerializationController serializer;

  public WebClientService(SerializationController serializer) {
    this.webServerClient = WebClientSingleton.getInstance();
    this.serializer = serializer;
    LOG.debug("WebClientService initialized with base web server URL: {}", BASE_URL);
  }

  /**
   * Sends results to the server.
   *
   * @param results the set of results to send. Must extends {@link Result}
   * @param clazz the class type of the results
   */
  public <T extends Result> void sendResult(Set<T> results, Class<T> clazz) {
    if (CollectionUtils.isEmpty(results)) return;
    LOG.info("Attempting to send {} results of type {} to the server", results.size(), clazz.getSimpleName());
    String json = serializer.serialize(results);
    String endpoint = resolveEndpoint(clazz);
    postJson(endpoint, json);
  }

  private String resolveEndpoint(Class<?> clazz) {
    String endpoint;
    switch (clazz.getSimpleName()) {
      case "PingResult":
        endpoint = PING_ENDPOINT;
        break;
      case "TelnetResult":
        endpoint = TELNET_ENDPOINT;
        break;
      default: throw new IllegalArgumentException("Cannot send results to the server! Unsupported result type: " + clazz.getName());
    }
    LOG.debug("Resolved endpoint: {} for class: {}", endpoint, clazz.getSimpleName());
    return endpoint;
  }

  /**
   * Sends POST to the server.
   *
   * @param url the endpoint URL
   * @param json the JSON payload
   * @throws WebClientException when connection to the server cannot be established
   */
  private void postJson(String url, String json) {
    LOG.debug("Posting JSON to URL: {}. Payload: {}", url, json);
    RequestBody body = RequestBody.create(json, MediaType.get(JSON_MEDIA_TYPE));
    Request request = new Request.Builder()
      .url(url)
      .addHeader("Authorization", "Bearer some-token") // TODO: implement JWT Client
      .post(body)
      .build();

    try {
      webServerClient.newCall(request).execute().close();
      LOG.debug("Successfully posted results to the server at URL: {}", url);
    } catch (IOException e) {
      throw new WebClientException("Cannot connect to the server", e);
    }
  }

  /**
   * Fetches the list of {@link NetworkConfig} from the server.
   *
   * @return set of config fetched from the server
   */
  public Set<NetworkConfig> fetchNetworkConfig() {
    LOG.debug("Fetching network configuration from the server");
    Request request = new Request.Builder().url(BASE_URL + "/config/network")
      .addHeader("Authorization", "Bearer some-token") // TODO: implement JWT Client
      .get()
      .build();
    return getSetOfObjects(request, NetworkConfig.class);
  }

  /**
   * Fetches the list of {@link Device} from the server.
   *
   * @return set of devices fetched from the server
   */
  public Set<Device> fetchDevices() {
    LOG.debug("Fetching devices from the server");
    Request request = new Request.Builder()
      .url(DEVICES_ENDPOINT)
      .addHeader("Authorization", "Bearer some-token") // TODO: implement JWT Client
      .get()
      .build();
    return getSetOfObjects(request, Device.class);
  }

  private <T> Set<T> getSetOfObjects(Request request, Class<T> clazz) {
    try (Response response = webServerClient.newCall(request).execute()) {
      if (!response.isSuccessful() || Objects.isNull(response.body())) {
        return Collections.emptySet();
      }
      String json = response.body().string();
      return serializer.deserializeToSet(json, clazz);
    } catch (IOException e) {
      throw new WebClientException("Error while fetching devices from server: " + e.getMessage(), e);
    }
  }
}
