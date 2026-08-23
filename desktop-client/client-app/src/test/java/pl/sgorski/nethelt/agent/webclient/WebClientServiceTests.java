package pl.sgorski.nethelt.agent.webclient;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Set;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.sgorski.nethelt.agent.exception.WebClientException;
import pl.sgorski.nethelt.agent.model.*;
import pl.sgorski.nethelt.agent.test_utils.TestDeviceFactory;
import pl.sgorski.nethelt.agent.test_utils.TestResultFactory;
import tools.jackson.databind.json.JsonMapper;

@SuppressWarnings("resource")
@ExtendWith(MockitoExtension.class)
class WebClientServiceTests {

  @Mock private OkHttpClient webServerClient;

  @Mock private Call call;

  @Mock private Response response;

  private final JsonMapper jsonMapper = JsonMapper.builder().build();

  private WebClientService webClientService;

  @BeforeEach
  void setUp() {
    webClientService = new WebClientService(webServerClient, jsonMapper);
  }

  @Test
  void sendResult_Success_PingResult() throws Exception {
    mockSuccessfulPost();

    var results = Set.of(TestResultFactory.createPingResult(true));

    webClientService.sendResult(results, PingResult.class);

    verify(webServerClient).newCall(any());
    verify(call).execute();
    verify(response).close();
  }

  @Test
  void sendResult_Success_TelnetResult() throws Exception {
    mockSuccessfulPost();

    var device = new Device("Device1", "192.168.1.2");
    var results = Set.of(new TelnetResult(device, true, "Message", 100L, true));

    webClientService.sendResult(results, TelnetResult.class);

    verify(webServerClient).newCall(any());
    verify(call).execute();
    verify(response).close();
  }

  @Test
  void sendResult_EmptySet_ShouldNotPost() {
    var results = Set.<PingResult>of();

    webClientService.sendResult(results, PingResult.class);

    verify(webServerClient, never()).newCall(any());
    verifyNoInteractions(call, response);
  }

  @Test
  void sendResult_IllegalEndpoint_ShouldThrow() {
    var results =
        Set.of(
            new NotConfiguredResult(
                TestDeviceFactory.createDeviceWithoutPort(), true, "message", 999));

    assertThrows(
        IllegalArgumentException.class,
        () -> webClientService.sendResult(results, NotConfiguredResult.class));

    verify(webServerClient, never()).newCall(any());
    verifyNoInteractions(call, response);
  }

  @Test
  void sendResult_IOException_ShouldThrow() throws Exception {
    when(webServerClient.newCall(any())).thenReturn(call);
    when(call.execute()).thenThrow(new IOException("IO Exception"));

    var results = Set.of(TestResultFactory.createPingResult(true));

    assertThrows(
        WebClientException.class, () -> webClientService.sendResult(results, PingResult.class));

    verify(webServerClient).newCall(any());
    verify(call).execute();
    verify(response, never()).close();
  }

  @Test
  void fetchNetworkConfig_Success() throws Exception {
    var responseBody = mock(ResponseBody.class);

    mockSuccessfulGet(responseBody);
    when(responseBody.string()).thenReturn("[{}]");

    var result = webClientService.fetchNetworkConfig();

    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertTrue(result.iterator().next() instanceof NetworkConfig);

    verify(webServerClient).newCall(any());
    verify(call).execute();
  }

  @Test
  void fetchNetworkConfig_IOException_ShouldThrow() throws Exception {
    when(webServerClient.newCall(any())).thenReturn(call);
    when(call.execute()).thenThrow(new IOException("IO Exception"));

    assertThrows(WebClientException.class, () -> webClientService.fetchNetworkConfig());

    verify(webServerClient).newCall(any());
    verify(call).execute();
  }

  @Test
  void fetchNetworkConfig_InvalidJson_ShouldThrow() throws Exception {
    var responseBody = mock(ResponseBody.class);

    mockSuccessfulGet(responseBody);
    when(responseBody.string()).thenReturn("invalid-json");

    assertThrows(Exception.class, () -> webClientService.fetchNetworkConfig());

    verify(webServerClient).newCall(any());
    verify(call).execute();
  }

  @Test
  void fetchNetworkConfig_NullResponseBody_ShouldReturnEmptySet() throws Exception {
    mockSuccessfulGet(null);

    var result = webClientService.fetchNetworkConfig();

    assertNotNull(result);
    assertTrue(result.isEmpty());

    verify(webServerClient).newCall(any());
    verify(call).execute();
  }

  @Test
  void fetchNetworkConfig_NotSuccessfulCall_ShouldReturnEmptySet() throws Exception {
    when(webServerClient.newCall(any())).thenReturn(call);
    when(call.execute()).thenReturn(response);
    when(response.isSuccessful()).thenReturn(false);

    var result = webClientService.fetchNetworkConfig();

    assertNotNull(result);
    assertTrue(result.isEmpty());

    verify(webServerClient).newCall(any());
    verify(call).execute();
  }

  @Test
  void fetchDevices_Success() throws Exception {
    var responseBody = mock(ResponseBody.class);

    mockSuccessfulGet(responseBody);
    when(responseBody.string()).thenReturn("[{}]");

    var result = webClientService.fetchDevices();

    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertTrue(result.iterator().next() instanceof Device);

    verify(webServerClient).newCall(any());
    verify(call).execute();
  }

  @Test
  void fetchDevices_IOException_ShouldThrow() throws Exception {
    when(webServerClient.newCall(any())).thenReturn(call);
    when(call.execute()).thenThrow(new IOException("IO Exception"));

    assertThrows(WebClientException.class, () -> webClientService.fetchDevices());

    verify(webServerClient).newCall(any());
    verify(call).execute();
  }

  @Test
  void fetchDevices_InvalidJson_ShouldThrow() throws Exception {
    var responseBody = mock(ResponseBody.class);

    mockSuccessfulGet(responseBody);
    when(responseBody.string()).thenReturn("invalid-json");

    assertThrows(Exception.class, () -> webClientService.fetchDevices());

    verify(webServerClient).newCall(any());
    verify(call).execute();
  }

  @Test
  void fetchDevices_NullResponseBody_ShouldReturnEmptySet() throws Exception {
    mockSuccessfulGet(null);

    var result = webClientService.fetchDevices();

    assertNotNull(result);
    assertTrue(result.isEmpty());

    verify(webServerClient).newCall(any());
    verify(call).execute();
  }

  @Test
  void fetchDevices_NotSuccessfulCall_ShouldReturnEmptySet() throws Exception {
    when(webServerClient.newCall(any())).thenReturn(call);
    when(call.execute()).thenReturn(response);
    when(response.isSuccessful()).thenReturn(false);

    var result = webClientService.fetchDevices();

    assertNotNull(result);
    assertTrue(result.isEmpty());

    verify(webServerClient).newCall(any());
    verify(call).execute();
  }

  private void mockSuccessfulPost() throws Exception {
    when(webServerClient.newCall(any())).thenReturn(call);
    when(call.execute()).thenReturn(response);
  }

  private void mockSuccessfulGet(ResponseBody responseBody) throws Exception {
    when(webServerClient.newCall(any())).thenReturn(call);
    when(call.execute()).thenReturn(response);
    when(response.isSuccessful()).thenReturn(true);
    when(response.body()).thenReturn(responseBody);
  }

  private static class NotConfiguredResult extends Result {

    protected NotConfiguredResult(
        Device device, boolean success, String message, long responseTimeMs) {
      super(device, success, message, responseTimeMs);
    }
  }
}
