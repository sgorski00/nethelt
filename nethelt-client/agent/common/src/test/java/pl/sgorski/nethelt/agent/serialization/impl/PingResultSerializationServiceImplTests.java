package pl.sgorski.nethelt.agent.serialization.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import pl.sgorski.nethelt.agent.serialization.SerializationService;
import pl.sgorski.nethelt.exception.SerializationException;
import pl.sgorski.nethelt.model.PingResult;

public class PingResultSerializationServiceImplTests {

  private ObjectMapper objectMapper;
  private SerializationService<PingResult> service;

  @BeforeEach
  void setUp() {
    objectMapper = mock(ObjectMapper.class);
    service = new PingResultSerializationServiceImpl(objectMapper);
  }

  @Test
  void toJson_SingleObject_ShouldReturnJson() throws Exception {
    String expected = "{}";
    when(objectMapper.writeValueAsString(any(PingResult.class))).thenReturn(expected);

    String result = service.toJson(new PingResult());

    assertEquals(expected, result);
  }

  @Test
  void toJson_SingleObject_ShouldThrow() throws Exception {
    when(objectMapper.writeValueAsString(any(PingResult.class))).thenThrow(JsonProcessingException.class);

    assertThrows(SerializationException.class, () -> service.toJson(new PingResult()));
  }

  @Test
  void toJson_Iterable_ShouldReturnJson() throws Exception {
    String expected = "[{}]";
    when(objectMapper.writeValueAsString(any(Iterable.class))).thenReturn(expected);

    String result = service.toJson(new ArrayList<>());

    assertEquals(expected, result);
  }

  @Test
  void toJson_Iterable_ShouldThrow() throws Exception {
    when(objectMapper.writeValueAsString(any(Iterable.class))).thenThrow(JsonProcessingException.class);

    assertThrows(SerializationException.class, () -> service.toJson(new ArrayList<>()));
  }

  @Test
  void toObject_ShouldReturnObject() throws Exception {
    String json = "{}";
    PingResult expected = new PingResult();
    when(objectMapper.readValue(anyString(), eq(PingResult.class))).thenReturn(expected);

    PingResult result = service.toObject(json);

    assertEquals(expected, result);
  }

  @Test
  void toObject_ShouldThrow() throws Exception {
    when(objectMapper.readValue(anyString(), eq(PingResult.class))).thenThrow(JsonProcessingException.class);

    assertThrows(SerializationException.class, () -> service.toObject("[{}]"));
  }

  @Test
  void toObjectSet_ShouldReturnObjectSet() throws Exception {
    String json = "[{}]";
    Set<PingResult> expected = Set.of();
    when(objectMapper.readValue(anyString(), ArgumentMatchers.<TypeReference<Set<PingResult>>>any())).thenReturn(expected);

    Set<PingResult> result = service.toObjectSet(json);
    Assertions.assertIterableEquals(expected, result);
  }

  @Test
  void toObjectSet_ShouldThrow() throws Exception {
    when(objectMapper.readValue(anyString(), ArgumentMatchers.<TypeReference<Set<PingResult>>>any()))
      .thenThrow(JsonProcessingException.class);

    assertThrows(SerializationException.class, () -> service.toObjectSet("[{}]"));
  }
}
