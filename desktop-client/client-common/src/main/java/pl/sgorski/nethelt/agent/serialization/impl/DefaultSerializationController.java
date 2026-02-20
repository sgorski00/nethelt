package pl.sgorski.nethelt.agent.serialization.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public final class DefaultSerializationController implements SerializationController {

  private final Map<Class<?>, SerializationService<?>> serializers = new HashMap<>();

  public DefaultSerializationController() {
    log.info("Initializing DefaultSerializationController");
    serializers.put(Device.class, new DeviceSerializationServiceImpl());
    serializers.put(PingResult.class, new PingResultSerializationServiceImpl());
    serializers.put(TelnetResult.class, new TelnetResultSerializationServiceImpl());
    serializers.put(NetworkConfig.class, new NetworkConfigSerializationServiceImpl());
    log.info("Registered serializers for classes: {}", serializers.keySet());
  }

  /**
   * Constructor for serializers injections, primarily for testing purposes.
   * In production it is recommended to use the default constructor.
   */
  public DefaultSerializationController(Map<Class<?>, SerializationService<?>> serializers) {
    log.warn("Using DefaultSerializationController constructor with custom serializers. This is intended for testing purposes only.");
    this.serializers.putAll(serializers);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> String serialize(Iterable<T> objects) {
    if(CollectionUtils.isEmpty(objects)) throw new SerializationException("Cannot serialize empty collection");
    var it = objects.iterator();
    var first = it.next();

    var serializer = (SerializationService<T>) serializers.get(first.getClass());
    if(Objects.isNull(serializer)) throw new SerializationException("No serializer found for class: " + first.getClass().getName());
    log.debug("Serializing Iterable<{}> to JSON", first.getClass().getSimpleName());
    return serializer.toJson(objects);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> String serialize(T object) {
    if(Objects.isNull(object)) throw new SerializationException("Cannot serialize null object");
    var serializer = (SerializationService<T>) serializers.get(object.getClass());
    if(Objects.isNull(serializer)) throw new SerializationException("No serializer found for class: " + object.getClass().getName());
    log.debug("Serializing {} to JSON", object.getClass().getSimpleName());
    return serializer.toJson(object);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> Set<T> deserializeToSet(String json, Class<T> type) {
    var serializer = (SerializationService<T>) serializers.get(type);
    if (Objects.isNull(serializer)) throw new SerializationException("No serializer found for class: " + type.getName());

    try {
      log.debug("Deserializing JSON to Set<{}>: {}", type.getSimpleName(), json);
      return serializer.toObjectSet(json);
    } catch (Exception e) {
      throw new SerializationException("Failed to deserialize JSON", e);
    }
  }
}
