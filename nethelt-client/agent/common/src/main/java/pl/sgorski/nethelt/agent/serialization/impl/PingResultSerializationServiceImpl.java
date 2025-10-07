package pl.sgorski.nethelt.agent.serialization.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.Set;
import pl.sgorski.nethelt.agent.config.ObjectMapperSingleton;
import pl.sgorski.nethelt.agent.serialization.ResultSerializationService;
import pl.sgorski.nethelt.exception.SerializationException;
import pl.sgorski.nethelt.model.PingResult;

/**
 * Service implementation for serializing and deserializing PingResult objects to and from JSON.
 */
public class PingResultSerializationServiceImpl implements ResultSerializationService<PingResult> {

  private final ObjectMapper objectMapper;

  public PingResultSerializationServiceImpl() {
    this.objectMapper = ObjectMapperSingleton.getInstance();
  }

  @Override
  public String toJson(PingResult object) {
    try {
      return objectMapper.writeValueAsString(object);
    } catch (JsonProcessingException ex) {
      throw new SerializationException("Failed to serialize PingResult to JSON", ex);
    }
  }

  @Override
  public String toJson(Iterable<PingResult> objects) throws SerializationException {
    try {
      return objectMapper.writeValueAsString(objects);
    } catch (JsonProcessingException e) {
      throw new SerializationException("Failed to serialize set of PingResults to JSON", e);
    }
  }

  @Override
  public PingResult toObject(String json) {
    try {
      return objectMapper.readValue(json, PingResult.class);
    } catch (JsonProcessingException e) {
      throw new SerializationException("Failed to deserialize JSON to PingResult", e);
    }
  }

  @Override
  public Set<PingResult> toObjectSet(String json) throws SerializationException {
    try {
      return objectMapper.readValue(json, new TypeReference<Set<PingResult>>() {});
    } catch (JsonProcessingException e) {
      throw new SerializationException("Failed to deserialize JSON to PingResults set", e);
    }
  }
}
