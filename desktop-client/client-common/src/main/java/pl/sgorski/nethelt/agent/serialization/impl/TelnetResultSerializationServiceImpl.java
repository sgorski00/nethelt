package pl.sgorski.nethelt.agent.serialization.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import pl.sgorski.nethelt.agent.config.ObjectMapperSingleton;
import pl.sgorski.nethelt.agent.serialization.ResultSerializationService;
import pl.sgorski.nethelt.exception.SerializationException;
import pl.sgorski.nethelt.model.TelnetResult;

/**
 * Service implementation for serializing and deserializing TelnetResult objects to and from JSON.
 */
@Slf4j
public final class TelnetResultSerializationServiceImpl implements ResultSerializationService<TelnetResult> {

  private final ObjectMapper objectMapper;

  public TelnetResultSerializationServiceImpl() {
    this.objectMapper = ObjectMapperSingleton.INSTANCE.getMapper();
  }

  /**
   * Constructor for dependency injection, primarily for testing purposes.
   * In production it is recommended to use the default constructor.
   */
  public TelnetResultSerializationServiceImpl(ObjectMapper objectMapper) {
    log.warn("Using TelnetResultSerializationServiceImpl constructor with custom ObjectMapper. This is intended for testing purposes only.");
    this.objectMapper = objectMapper;
  }

  @Override
  public String toJson(TelnetResult object) {
    try {
      return objectMapper.writeValueAsString(object);
    } catch (JsonProcessingException ex) {
      throw new SerializationException("Failed to serialize TelnetResult to JSON", ex);
    }
  }

  @Override
  public String toJson(Iterable<TelnetResult> objects) throws SerializationException {
    try {
      return objectMapper.writeValueAsString(objects);
    } catch (JsonProcessingException e) {
      throw new SerializationException("Failed to serialize set of PingResults to JSON", e);
    }
  }

  @Override
  public TelnetResult toObject(String json) {
    try {
      return objectMapper.readValue(json, TelnetResult.class);
    } catch (JsonProcessingException e) {
      throw new SerializationException("Failed to deserialize JSON to TelnetResult", e);
    }
  }

  @Override
  public Set<TelnetResult> toObjectSet(String json) throws SerializationException {
    try {
      return objectMapper.readValue(json, new TypeReference<>() {});
    } catch (JsonProcessingException e) {
      throw new SerializationException("Failed to deserialize JSON to PingResults set", e);
    }
  }
}
