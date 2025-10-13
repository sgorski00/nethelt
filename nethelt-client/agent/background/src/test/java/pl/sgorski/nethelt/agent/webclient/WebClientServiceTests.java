package pl.sgorski.nethelt.agent.webclient;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import pl.gorski.nethelt.agent.config.WebClientSingleton;
import pl.gorski.nethelt.agent.webclient.WebClientService;
import pl.sgorski.nethelt.agent.serialization.SerializationController;
import pl.sgorski.nethelt.exception.SerializationException;
import pl.sgorski.nethelt.exception.WebClientException;
import pl.sgorski.nethelt.model.NetworkConfig;
import pl.sgorski.nethelt.model.PingResult;
import pl.sgorski.nethelt.model.Result;
import pl.sgorski.nethelt.model.TelnetResult;

@SuppressWarnings("resource")
public class WebClientServiceTests {

  private OkHttpClient webServerClient;
  private Call call;
  private Response response;
  private SerializationController serializer;
  private WebClientService webClientService;

  @BeforeEach
  void setUp() throws Exception {
    webServerClient = mock(OkHttpClient.class);
    call = mock(Call.class);
    response = mock(Response.class);
    when(webServerClient.newCall(any())).thenReturn(call);
    when(call.execute()).thenReturn(response);
    serializer = mock(SerializationController.class);
    try(MockedStatic<?> webClient = mockStatic(WebClientSingleton.class)) {
      webClient.when(WebClientSingleton::getInstance).thenReturn(webServerClient);
      webClientService = new WebClientService(serializer);
    }
  }

  @Test
  void sendResult_Success_PingResult() throws Exception {
    Set<PingResult> results = Set.of(new PingResult());
    when(serializer.serialize(anySet())).thenReturn("[{}]");

    webClientService.sendResult(results, PingResult.class);

    verify(webServerClient, times(1)).newCall(any());
    verify(call, times(1)).execute();
    verify(response, times(1)).close();
  }

  @Test
  void sendResult_Success_TelnetResult() throws Exception {
    Set<TelnetResult> results = Set.of(new TelnetResult());
    when(serializer.serialize(anySet())).thenReturn("[{}]");

    webClientService.sendResult(results, TelnetResult.class);

    verify(webServerClient, times(1)).newCall(any());
    verify(call, times(1)).execute();
    verify(response, times(1)).close();
  }

  @Test
  void sendResult_EmptySet_ShouldNotPost() throws Exception {
    Set<PingResult> results = Set.of();

    webClientService.sendResult(results, PingResult.class);

    verify(webServerClient, never()).newCall(any());
    verify(call, never()).execute();
    verify(response, never()).close();
  }

  @Test
  void sendResult_SerializationException_ShouldThrow() throws Exception {
    Set<PingResult> results = Set.of(new PingResult());
    when(serializer.serialize(anySet())).thenThrow(new SerializationException("Serialization Exception"));

    assertThrows(SerializationException.class, () -> webClientService.sendResult(results, PingResult.class));

    verify(webServerClient, never()).newCall(any());
    verify(call, never()).execute();
    verify(response, never()).close();
  }

  @Test
  void sendResult_IllegalEndpoint_ShouldThrow() throws Exception {
    Set<NotConfiguredResult> results = Set.of(new NotConfiguredResult());
    when(serializer.serialize(anySet())).thenReturn("[{}]");

    assertThrows(IllegalArgumentException.class, () -> webClientService.sendResult(results, NotConfiguredResult.class));

    verify(webServerClient, never()).newCall(any());
    verify(call, never()).execute();
    verify(response, never()).close();
  }

  @Test
  void sendResult_IOException_ShouldThrow() throws Exception {
    Set<PingResult> results = Set.of(new PingResult());
    when(serializer.serialize(anySet())).thenReturn("[{}]");
    when(call.execute()).thenThrow(new IOException("IO Exception"));

    assertThrows(WebClientException.class, () -> webClientService.sendResult(results, PingResult.class));

    verify(webServerClient, times(1)).newCall(any());
    verify(call, times(1)).execute();
    verify(response, never()).close();
  }

  @Test
  void fetchNetworkConfig_Success() throws Exception {
    ResponseBody responseBody = mock(ResponseBody.class);
    Set<NetworkConfig> expectedSet = Set.of(new NetworkConfig());

    when(response.isSuccessful()).thenReturn(true);
    when(response.body()).thenReturn(responseBody);
    when(responseBody.string()).thenReturn("[{}]");
    when(serializer.deserializeToSet(anyString(), any())).thenReturn(Collections.singleton(expectedSet));

    Set<?> result = webClientService.fetchNetworkConfig();

    assertNotNull(result);
    assertFalse(result.isEmpty());
    verify(webServerClient, times(1)).newCall(any());
    verify(call, times(1)).execute();
    verify(serializer, times(1)).deserializeToSet(anyString(), any());
  }

  @Test
  void fetchNetworkConfig_IOException_ShouldThrow() throws Exception {
    when(call.execute()).thenThrow(new IOException("IO Exception"));

    assertThrows(WebClientException.class, () -> webClientService.fetchNetworkConfig());

    verify(webServerClient, times(1)).newCall(any());
    verify(call, times(1)).execute();
    verify(serializer, never()).deserializeToSet(anyString(), any());
  }

  @Test
  void fetchNetworkConfig_DeserializationException_ShouldThrow() throws Exception {
    ResponseBody responseBody = mock(ResponseBody.class);
    when(response.isSuccessful()).thenReturn(true);
    when(response.body()).thenReturn(responseBody);
    when(responseBody.string()).thenReturn("[{}]");
    when(serializer.deserializeToSet(anyString(), any())).thenThrow(new SerializationException("Deserialization Exception"));

    assertThrows(SerializationException.class, () -> webClientService.fetchNetworkConfig());

    verify(webServerClient, times(1)).newCall(any());
    verify(call, times(1)).execute();
    verify(serializer, times(1)).deserializeToSet(anyString(), any());
  }

  @Test
  void fetchNetworkConfig_NullResponseBody_ShouldReturnEmptySet() throws Exception {
    when(response.isSuccessful()).thenReturn(true);
    when(response.body()).thenReturn(null);

    Set<?> result = webClientService.fetchNetworkConfig();

    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(webServerClient, times(1)).newCall(any());
    verify(call, times(1)).execute();
    verify(serializer, never()).deserializeToSet(anyString(), any());
  }

  @Test
  void fetchNetworkConfig_NotSuccessfulCall_ShouldReturnEmptySet() throws Exception {
    when(response.isSuccessful()).thenReturn(false);

    Set<?> result = webClientService.fetchNetworkConfig();

    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(webServerClient, times(1)).newCall(any());
    verify(call, times(1)).execute();
    verify(serializer, never()).deserializeToSet(anyString(), any());
  }

  @Test
  void fetchDevices_Success() throws Exception {
    ResponseBody responseBody = mock(ResponseBody.class);
    Set<NetworkConfig> expectedSet = Set.of(new NetworkConfig());

    when(response.isSuccessful()).thenReturn(true);
    when(response.body()).thenReturn(responseBody);
    when(responseBody.string()).thenReturn("[{}]");
    when(serializer.deserializeToSet(anyString(), any())).thenReturn(Collections.singleton(expectedSet));

    Set<?> result = webClientService.fetchDevices();

    assertNotNull(result);
    assertFalse(result.isEmpty());
    verify(webServerClient, times(1)).newCall(any());
    verify(call, times(1)).execute();
    verify(serializer, times(1)).deserializeToSet(anyString(), any());
  }

  @Test
  void fetchDevices_IOException_ShouldThrow() throws Exception {
    when(call.execute()).thenThrow(new IOException("IO Exception"));

    assertThrows(WebClientException.class, () -> webClientService.fetchDevices());

    verify(webServerClient, times(1)).newCall(any());
    verify(call, times(1)).execute();
    verify(serializer, never()).deserializeToSet(anyString(), any());
  }

  @Test
  void fetchDevices_DeserializationException_ShouldThrow() throws Exception {
    ResponseBody responseBody = mock(ResponseBody.class);
    when(response.isSuccessful()).thenReturn(true);
    when(response.body()).thenReturn(responseBody);
    when(responseBody.string()).thenReturn("[{}]");
    when(serializer.deserializeToSet(anyString(), any())).thenThrow(new SerializationException("Deserialization Exception"));

    assertThrows(SerializationException.class, () -> webClientService.fetchDevices());

    verify(webServerClient, times(1)).newCall(any());
    verify(call, times(1)).execute();
    verify(serializer, times(1)).deserializeToSet(anyString(), any());
  }

  @Test
  void fetchDevices_NullResponseBody_ShouldReturnEmptySet() throws Exception {
    when(response.isSuccessful()).thenReturn(true);
    when(response.body()).thenReturn(null);

    Set<?> result = webClientService.fetchDevices();

    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(webServerClient, times(1)).newCall(any());
    verify(call, times(1)).execute();
    verify(serializer, never()).deserializeToSet(anyString(), any());
  }

  @Test
  void fetchDevices_NotSuccessfulCall_ShouldReturnEmptySet() throws Exception {
    when(response.isSuccessful()).thenReturn(false);

    Set<?> result = webClientService.fetchDevices();

    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(webServerClient, times(1)).newCall(any());
    verify(call, times(1)).execute();
    verify(serializer, never()).deserializeToSet(anyString(), any());
  }

  private static class NotConfiguredResult extends Result { }
}
