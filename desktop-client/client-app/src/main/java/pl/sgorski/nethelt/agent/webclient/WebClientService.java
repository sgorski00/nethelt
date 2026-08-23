package pl.sgorski.nethelt.agent.webclient;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import pl.sgorski.nethelt.agent.exception.WebClientException;
import pl.sgorski.nethelt.agent.model.Device;
import pl.sgorski.nethelt.agent.model.NetworkConfig;
import pl.sgorski.nethelt.agent.model.Result;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebClientService {

  private static final String BASE_URL = "http://localhost:8080/api";
  private static final String DEVICES_ENDPOINT = BASE_URL + "/devices";
  private static final String PING_ENDPOINT = BASE_URL + "/ping";
  private static final String TELNET_ENDPOINT = BASE_URL + "/telnet";
  private static final String JSON_MEDIA_TYPE = "application/json";

  private final OkHttpClient webServerClient;
  private final JsonMapper jsonMapper;

  public <T extends Result> void sendResult(Set<T> results, Class<T> clazz) {
    if (CollectionUtils.isEmpty(results)) return;
    log.info(
        "Attempting to send {} results of type {} to the server",
        results.size(),
        clazz.getSimpleName());
    var json = jsonMapper.writeValueAsString(results);
    var endpoint = resolveEndpoint(clazz);
    log.debug("Resolved endpoint: {} for class: {}", endpoint, clazz.getSimpleName());
    postJson(endpoint, json);
  }

  private String resolveEndpoint(Class<?> clazz) {
    return switch (clazz.getSimpleName()) {
      case "PingResult" -> PING_ENDPOINT;
      case "TelnetResult" -> TELNET_ENDPOINT;
      default ->
          throw new IllegalArgumentException(
              "Cannot send results to the server! Unsupported result type: " + clazz.getName());
    };
  }

  private void postJson(String url, String json) {
    log.debug("Posting JSON to URL: {}. Payload: {}", url, json);
    var body = RequestBody.create(json, MediaType.get(JSON_MEDIA_TYPE));
    var request =
        new Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer some-token") // TODO: implement JWT Client
            .post(body)
            .build();

    try {
      webServerClient.newCall(request).execute().close();
      log.debug("Successfully posted results to the server at URL: {}", url);
    } catch (IOException e) {
      throw new WebClientException("Cannot connect to the server", e);
    }
  }

  public Set<NetworkConfig> fetchNetworkConfig() {
    log.debug("Fetching network configuration from the server");
    var request =
        new Request.Builder()
            .url(BASE_URL + "/config/network")
            .addHeader("Authorization", "Bearer some-token") // TODO: implement JWT Client
            .get()
            .build();
    return getSetOfObjects(request, NetworkConfig.class);
  }

  public Set<Device> fetchDevices() {
    log.debug("Fetching devices from the server");
    var request =
        new Request.Builder()
            .url(DEVICES_ENDPOINT)
            .addHeader("Authorization", "Bearer some-token") // TODO: implement JWT Client
            .get()
            .build();
    return getSetOfObjects(request, Device.class);
  }

  private <T> Set<T> getSetOfObjects(Request request, Class<T> clazz) {
    try (var response = webServerClient.newCall(request).execute()) {
      if (!response.isSuccessful() || Objects.isNull(response.body())) {
        return Set.of();
      }
      return jsonMapper.readValue(
          response.body().string(),
          jsonMapper.getTypeFactory().constructCollectionType(Set.class, clazz));
    } catch (IOException e) {
      throw new WebClientException(
          "Error while fetching devices from server: " + e.getMessage(), e);
    }
  }
}
