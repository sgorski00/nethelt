package pl.sgorski.nethelt.agent.serialization.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.sgorski.nethelt.agent.serialization.SerializationService;
import pl.sgorski.nethelt.exception.SerializationException;
import pl.sgorski.nethelt.model.Device;

public class DefaultSerializationControllerTests {
  private SerializationService<Device> deviceSerializer;
  private DefaultSerializationController controller;

  @BeforeEach
  void setUp() {
    deviceSerializer = mock(SerializationService.class);
    Map<Class<?>, SerializationService<?>> serializers = new HashMap<>();
    serializers.put(Device.class, deviceSerializer);

    controller = new DefaultSerializationController(serializers);
  }

  @Test
  void serializeSingleObject_ShouldThrow_NullObject() {
    var ex = assertThrows(SerializationException.class, () -> controller.serialize((Object) null));
    assertTrue(ex.getMessage().contains("Cannot serialize null object"));
  }

  @Test
  void serializeSingleObject_ShouldThrow_SerializerNotFound() {
    var notSerializable = new NotSerializable();

    var ex = assertThrows(SerializationException.class, () -> controller.serialize(notSerializable));
    assertTrue(ex.getMessage().contains("No serializer found for class"));
  }

  @Test
  void serializeSingleObject_ShouldReturnJson_SerializerExists() {
    var expected = "{\"name\":\"Device\"}";
    when(deviceSerializer.toJson(any(Device.class))).thenReturn(expected);

    var result = controller.serialize(new Device());

    assertEquals(expected, result);
  }

  @Test
  void serializeIterable_ShouldThrow_EmptyCollection() {
    var ex = assertThrows(SerializationException.class, () -> controller.serialize((Iterable<?>) null));
    assertTrue(ex.getMessage().contains("Cannot serialize empty collection"));
  }

  @Test
  void serializeIterable_ShouldThrow_SerializerNotFound() {
    var notSerializable = new NotSerializable();
    var list = List.of(notSerializable);

    var ex = assertThrows(SerializationException.class, () -> controller.serialize(list));
    assertTrue(ex.getMessage().contains("No serializer found for class"));
  }

  @Test
  void serializeIterable_ShouldReturnJson_SerializerExists() {
    var devices = List.of(new Device());
    var expected = "[{\"name\":\"Device\"}]";
    when(deviceSerializer.toJson(anyIterable())).thenReturn(expected);

    var result = controller.serialize(devices);

    assertEquals(expected, result);
  }

  @Test
  void deserializeToSet_ShouldDeserialize_SerializerExists() {
    var json = "[{\"name\":\"Device\"}]";
    var devices = Set.of(new Device());
    when(deviceSerializer.toObjectSet(anyString())).thenReturn(devices);

    var result = controller.deserializeToSet(json, Device.class);

    assertEquals(1, result.size());
  }

  @Test
  void deserializeToSet_ShouldThrow_SerializerNotFound() {
    var ex = assertThrows(SerializationException.class, () -> controller.deserializeToSet("{}", NotSerializable.class));

    assertTrue(ex.getMessage().contains("No serializer found for class"));
  }

  @Test
  void deserializeToSet_ShouldThrow_SerializationException() {
    var json = "[{\"name\":\"Device\"}]";
    when(deviceSerializer.toObjectSet(anyString())).thenThrow(new SerializationException("Something went wrong"));

    var ex = assertThrows(SerializationException.class, () -> controller.deserializeToSet(json, Device.class));

    assertTrue(ex.getMessage().contains("Failed to deserialize JSON"));
  }

  private static final class NotSerializable {
    public NotSerializable() { }
  }
}
