package pl.sgorski.nethelt.agent.serialization.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.Set;
import pl.sgorski.nethelt.agent.config.ObjectMapperSingleton;
import pl.sgorski.nethelt.agent.serialization.SerializationService;
import pl.sgorski.nethelt.exception.SerializationException;
import pl.sgorski.nethelt.model.Device;

/**
 * Implemented service for serializing and deserializing Device objects to and from JSON.
 */
public class DeviceSerializationServiceImpl implements SerializationService<Device> {

  private final ObjectMapper objectMapper;

  public DeviceSerializationServiceImpl() {
    this.objectMapper = ObjectMapperSingleton.getInstance();
  }

  @Override
  public String toJson(Device object) throws SerializationException {
    try {
      return objectMapper.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw new SerializationException("Failed to serialize Device to JSON", e);
    }
  }

  @Override
  public String toJson(Iterable<Device> objects) throws SerializationException {
    try {
      return objectMapper.writeValueAsString(objects);
    } catch (JsonProcessingException e) {
      throw new SerializationException("Failed to serialize set of Devices to JSON", e);
    }
  }

  @Override
  public Device toObject(String json) throws SerializationException {
    try {
      return objectMapper.readValue(json, Device.class);
    } catch (JsonProcessingException e) {
      throw new SerializationException("Failed to deserialize JSON to Device", e);
    }
  }

  @Override
  public Set<Device> toObjectSet(String json) throws SerializationException {
    try {
      return objectMapper.readValue(json, new TypeReference<Set<Device>>() {});
    } catch (JsonProcessingException e) {
      throw new SerializationException("Failed to deserialize JSON to Device set", e);
    }
  }
}
