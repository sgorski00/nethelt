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
import pl.sgorski.nethelt.model.TelnetResult;

public class TelnetResultSerializationServiceImplTests {

  private ObjectMapper objectMapper;
  private SerializationService<TelnetResult> service;

  @BeforeEach
  void setUp() {
    objectMapper = mock(ObjectMapper.class);
    service = new TelnetResultSerializationServiceImpl(objectMapper);
  }

  @Test
  void toJson_SingleObject_ShouldReturnJson() throws Exception {
    String expected = "{}";
    when(objectMapper.writeValueAsString(any(TelnetResult.class))).thenReturn(expected);

    String result = service.toJson(new TelnetResult());

    assertEquals(expected, result);
  }

  @Test
  void toJson_SingleObject_ShouldThrow() throws Exception {
    when(objectMapper.writeValueAsString(any(TelnetResult.class))).thenThrow(JsonProcessingException.class);

    assertThrows(SerializationException.class, () -> service.toJson(new TelnetResult()));
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
    TelnetResult expected = new TelnetResult();
    when(objectMapper.readValue(anyString(), eq(TelnetResult.class))).thenReturn(expected);

    TelnetResult result = service.toObject(json);

    assertEquals(expected, result);
  }

  @Test
  void toObject_ShouldThrow() throws Exception {
    when(objectMapper.readValue(anyString(), eq(TelnetResult.class))).thenThrow(JsonProcessingException.class);

    assertThrows(SerializationException.class, () -> service.toObject("[{}]"));
  }

  @Test
  void toObjectSet_ShouldReturnObjectSet() throws Exception {
    String json = "[{}]";
    Set<TelnetResult> expected = Set.of();
    when(objectMapper.readValue(anyString(), ArgumentMatchers.<TypeReference<Set<TelnetResult>>>any())).thenReturn(expected);

    Set<TelnetResult> result = service.toObjectSet(json);
    Assertions.assertIterableEquals(expected, result);
  }

  @Test
  void toObjectSet_ShouldThrow() throws Exception {
    when(objectMapper.readValue(anyString(), ArgumentMatchers.<TypeReference<Set<TelnetResult>>>any()))
      .thenThrow(JsonProcessingException.class);

    assertThrows(SerializationException.class, () -> service.toObjectSet("[{}]"));
  }
}
