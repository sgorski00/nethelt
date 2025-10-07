package pl.sgorski.nethelt.agent.serialization.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.sgorski.nethelt.agent.serialization.SerializationController;
import pl.sgorski.nethelt.agent.serialization.SerializationService;
import pl.sgorski.nethelt.exception.SerializationException;
import pl.sgorski.nethelt.model.Device;
import pl.sgorski.nethelt.model.NetworkConfig;
import pl.sgorski.nethelt.model.PingResult;
import pl.sgorski.nethelt.model.TelnetResult;
import pl.sgorski.nethelt.utils.CollectionUtils;

/**
 * Default implementation of SerializationController that manages different serializers for various object types.
 * It uses a map to associate classes with their respective SerializationService implementations.
 */
public class DefaultSerializationController implements SerializationController {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultSerializationController.class);

  private final Map<Class<?>, SerializationService<?>> serializers = new HashMap<>();

  public DefaultSerializationController() {
    LOG.info("Initializing DefaultSerializationController");
    serializers.put(Device.class, new DeviceSerializationServiceImpl());
    serializers.put(PingResult.class, new PingResultSerializationServiceImpl());
    serializers.put(TelnetResult.class, new TelnetResultSerializationServiceImpl());
    serializers.put(NetworkConfig.class, new NetworkConfigSerializationServiceImpl());
    LOG.info("Registered serializers for classes: {}", serializers.keySet());
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> String serialize(Iterable<T> objects) {
    if(CollectionUtils.isEmpty(objects)) throw new SerializationException("Cannot serialize empty collection");
    Iterator<T> it = objects.iterator();
    T first = it.next();

    SerializationService<T> serializer = (SerializationService<T>) serializers.get(first.getClass());
    if(Objects.isNull(serializer)) throw new SerializationException("No serializer found for class: " + first.getClass().getName());
    LOG.debug("Serializing Iterable<{}> to JSON", first.getClass().getSimpleName());
    return serializer.toJson(objects);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> String serialize(T object) {
    if(Objects.isNull(object)) throw new SerializationException("Cannot serialize null object");
    SerializationService<T> serializer = (SerializationService<T>) serializers.get(object.getClass());
    if(Objects.isNull(serializer)) throw new SerializationException("No serializer found for class: " + object.getClass().getName());
    LOG.debug("Serializing {} to JSON", object.getClass().getSimpleName());
    return serializer.toJson(object);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> Set<T> deserializeToSet(String json, Class<T> type) {
    SerializationService<T> serializer = (SerializationService<T>) serializers.get(type);
    if (Objects.isNull(serializer)) throw new SerializationException("No serializer found for class: " + type.getName());

    try {
      LOG.debug("Deserializing JSON to Set<{}>: {}", type.getSimpleName(), json);
      return serializer.toObjectSet(json);
    } catch (Exception e) {
      throw new SerializationException("Failed to deserialize JSON", e);
    }
  }
}
