package pl.sgorski.nethelt.agent.serialization.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Set;
import pl.sgorski.nethelt.agent.config.ObjectMapperSingleton;
import pl.sgorski.nethelt.agent.serialization.SerializationService;
import pl.sgorski.nethelt.exception.SerializationException;
import pl.sgorski.nethelt.model.NetworkConfig;

/**
 * Implemented service for serializing and deserializing {@link NetworkConfig} objects to and from JSON.
 */
public class NetworkConfigSerializationServiceImpl implements SerializationService<NetworkConfig> {

  private final ObjectMapper objectMapper;

  public NetworkConfigSerializationServiceImpl() {
    this.objectMapper = ObjectMapperSingleton.getInstance();
  }

  @Override
  public String toJson(NetworkConfig object) throws SerializationException {
    try {
      return objectMapper.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw new SerializationException("Failed to serialize Network Config to JSON", e);
    }
  }

  @Override
  public String toJson(Iterable<NetworkConfig> objects) throws SerializationException {
    try {
      return objectMapper.writeValueAsString(objects);
    } catch (JsonProcessingException e) {
      throw new SerializationException("Failed to serialize set of configs to JSON", e);
    }
  }

  @Override
  public NetworkConfig toObject(String json) throws SerializationException {
    try {
      return objectMapper.readValue(json, NetworkConfig.class);
    } catch (JsonProcessingException e) {
      throw new SerializationException("Failed to deserialize JSON to NetworkConfig", e);
    }
  }

  @Override
  public Set<NetworkConfig> toObjectSet(String json) throws SerializationException {
    try {
      return objectMapper.readValue(json, new TypeReference<Set<NetworkConfig>>() {});
    } catch (JsonProcessingException e) {
      throw new SerializationException("Failed to deserialize JSON to NetworkConfig set", e);
    }
  }
}
