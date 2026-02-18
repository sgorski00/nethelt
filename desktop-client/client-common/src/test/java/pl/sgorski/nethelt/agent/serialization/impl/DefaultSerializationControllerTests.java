package pl.sgorski.nethelt.agent.serialization.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
    Map<Class<?>, SerializationService<?>> serializers = Map.of(Device.class, deviceSerializer);

    controller = new DefaultSerializationController(serializers);
  }

  @Test
  void serializeSingleObject_ShouldThrow_NullObject() {
    SerializationException ex = assertThrows(SerializationException.class, () -> controller.serialize((Object) null));
    assertTrue(ex.getMessage().contains("Cannot serialize null object"));
  }

  @Test
  void serializeSingleObject_ShouldThrow_SerializerNotFound() {
    NotSerializable notSerializable = new NotSerializable();

    SerializationException ex = assertThrows(SerializationException.class, () -> controller.serialize(notSerializable));
    assertTrue(ex.getMessage().contains("No serializer found for class"));
  }

  @Test
  void serializeSingleObject_ShouldReturnJson_SerializerExists() {
    String expected = "{\"name\":\"Device\"}";
    when(deviceSerializer.toJson(any(Device.class))).thenReturn(expected);

    String result = controller.serialize(new Device());

    assertEquals(expected, result);
  }

  @Test
  void serializeIterable_ShouldThrow_EmptyCollection() {
    SerializationException ex = assertThrows(SerializationException.class, () -> controller.serialize((Iterable<?>) null));
    assertTrue(ex.getMessage().contains("Cannot serialize empty collection"));
  }

  @Test
  void serializeIterable_ShouldThrow_SerializerNotFound() {
    NotSerializable notSerializable = new NotSerializable();
    List<NotSerializable> list = List.of(notSerializable);

    SerializationException ex = assertThrows(SerializationException.class, () -> controller.serialize(list));
    assertTrue(ex.getMessage().contains("No serializer found for class"));
  }

  @Test
  void serializeIterable_ShouldReturnJson_SerializerExists() {
    List<Device> devices = List.of(new Device());
    String expected = "[{\"name\":\"Device\"}]";
    when(deviceSerializer.toJson(anyIterable())).thenReturn(expected);

    String result = controller.serialize(devices);

    assertEquals(expected, result);
  }

  @Test
  void deserializeToSet_ShouldDeserialize_SerializerExists() {
    String json = "[{\"name\":\"Device\"}]";
    Set<Device> devices = Set.of(new Device());
    when(deviceSerializer.toObjectSet(anyString())).thenReturn(devices);

    Set<Device> result = controller.deserializeToSet(json, Device.class);

    assertEquals(1, result.size());
  }

  @Test
  void deserializeToSet_ShouldThrow_SerializerNotFound() {
    SerializationException ex = assertThrows(SerializationException.class, () -> controller.deserializeToSet("{}", NotSerializable.class));

    assertTrue(ex.getMessage().contains("No serializer found for class"));
  }

  @Test
  void deserializeToSet_ShouldThrow_SerializationException() {
    String json = "[{\"name\":\"Device\"}]";
    when(deviceSerializer.toObjectSet(anyString())).thenThrow(new SerializationException("Something went wrong"));

    SerializationException ex = assertThrows(SerializationException.class, () -> controller.deserializeToSet(json, Device.class));

    assertTrue(ex.getMessage().contains("Failed to deserialize JSON"));
  }

  private static class NotSerializable {
    public NotSerializable() { }
  }
}
