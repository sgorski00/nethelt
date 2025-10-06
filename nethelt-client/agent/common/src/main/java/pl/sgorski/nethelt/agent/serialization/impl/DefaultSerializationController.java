package pl.sgorski.nethelt.agent.serialization.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import pl.sgorski.nethelt.agent.serialization.SerializationController;
import pl.sgorski.nethelt.agent.serialization.SerializationService;
import pl.sgorski.nethelt.core.exception.SerializationException;
import pl.sgorski.nethelt.core.model.Device;
import pl.sgorski.nethelt.core.model.NetworkConfig;
import pl.sgorski.nethelt.core.model.PingResult;
import pl.sgorski.nethelt.core.model.TelnetResult;
import pl.sgorski.nethelt.core.utils.CollectionUtils;

/**
 * Default implementation of SerializationController that manages different serializers for various object types.
 * It uses a map to associate classes with their respective SerializationService implementations.
 */
public class DefaultSerializationController implements SerializationController {

  private final Map<Class<?>, SerializationService<?>> serializers = new HashMap<>();

  public DefaultSerializationController() {
    serializers.put(Device.class, new DeviceSerializationServiceImpl());
    serializers.put(PingResult.class, new PingResultSerializationServiceImpl());
    serializers.put(TelnetResult.class, new TelnetResultSerializationServiceImpl());
    serializers.put(NetworkConfig.class, new NetworkConfigSerializationServiceImpl());
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> String serialize(Iterable<T> objects) {
    if(CollectionUtils.isEmpty(objects)) throw new SerializationException("Cannot serialize empty collection");
    Iterator<T> it = objects.iterator();
    T first = it.next();

    SerializationService<T> serializer = (SerializationService<T>) serializers.get(first.getClass());
    if(Objects.isNull(serializer)) throw new SerializationException("No serializer found for class: " + first.getClass().getName());
    return serializer.toJson(objects);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> String serialize(T object) {
    if(Objects.isNull(object)) throw new SerializationException("Cannot serialize null object");
    SerializationService<T> serializer = (SerializationService<T>) serializers.get(object.getClass());
    if(Objects.isNull(serializer)) throw new SerializationException("No serializer found for class: " + object.getClass().getName());
    return serializer.toJson(object);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> Set<T> deserializeToSet(String json, Class<T> type) {
    SerializationService<T> serializer = (SerializationService<T>) serializers.get(type);
    if (Objects.isNull(serializer)) throw new SerializationException("No serializer found for class: " + type.getName());

    try {
      return serializer.toObjectSet(json);
    } catch (Exception e) {
      throw new SerializationException("Failed to deserialize JSON", e);
    }
  }
}
