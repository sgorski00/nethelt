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
import pl.gorski.nethelt.agent.config.WebClientSingleton;
import pl.sgorski.nethelt.agent.serialization.SerializationController;
import pl.sgorski.nethelt.core.exception.WebClientException;
import pl.sgorski.nethelt.core.model.Device;
import pl.sgorski.nethelt.core.model.NetworkConfig;
import pl.sgorski.nethelt.core.model.Result;

//TODO: add logging
//TODO: add unit tests

/**
 * Service for communicating with the web server.
 */
public class WebClientService {

  private final static String BASE_URL = "http://localhost:8080/api";
  private final static String DEVICES_ENDPOINT = BASE_URL + "/devices";
  private final static String PING_ENDPOINT = BASE_URL + "/ping";
  private final static String TELNET_ENDPOINT = BASE_URL + "/telnet";
  private final static String JSON_MEDIA_TYPE = "application/json";

  private final OkHttpClient webServerClient;
  private final SerializationController serializer;

  public WebClientService(SerializationController serializer) {
    this.webServerClient = WebClientSingleton.getInstance();
    this.serializer = serializer;
  }

  /**
   * Sends results to the server.
   *
   * @param results the set of results to send. Must extends {@link Result}
   * @param clazz the class type of the results
   */
  public <T extends Result> void sendResult(Set<T> results, Class<T> clazz) {
    if (Objects.isNull(results) || results.isEmpty()) return;
    String json = serializer.serialize(results);
    String endpoint = resolveEndpoint(clazz);
    postJson(endpoint, json);
  }

  private String resolveEndpoint(Class<?> clazz) {
    switch (clazz.getSimpleName()) {
      case "PingResult":
        return PING_ENDPOINT;
      case "TelnetResult":
        return TELNET_ENDPOINT;
      default: throw new IllegalArgumentException("Cannot send results to the server! Unsupported result type: " + clazz.getName());
    }
  }

  /**
   * Sends POST to the server.
   *
   * @param url the endpoint URL
   * @param json the JSON payload
   * @throws WebClientException when connection to the server cannot be established
   */
  private void postJson(String url, String json) {
    RequestBody body = RequestBody.create(json, MediaType.get(JSON_MEDIA_TYPE));
    Request request = new Request.Builder()
      .url(url)
      .addHeader("Authorization", "Bearer some-token") // TODO: implement JWT Client
      .post(body)
      .build();

    try {
      webServerClient.newCall(request).execute().close();
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
