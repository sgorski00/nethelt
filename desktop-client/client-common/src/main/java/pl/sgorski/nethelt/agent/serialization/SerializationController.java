package pl.sgorski.nethelt.agent.serialization;

import java.util.Set;

/**
 * Generic controller for serializing and deserializing objects to and from JSON.
 */
public interface SerializationController {
  <T> String serialize(Iterable<T> objects);
  <T> String serialize(T object);

  <T> Set<T> deserializeToSet(String json, Class<T> type);
}
