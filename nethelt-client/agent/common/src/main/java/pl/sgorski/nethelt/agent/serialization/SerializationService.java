package pl.sgorski.nethelt.agent.serialization;

import java.util.Set;
import pl.sgorski.nethelt.core.exception.SerializationException;

/**
 * Service interface for serializing and deserializing objects to and from JSON.
 *
 * @param <O> the type of object to be serialized/deserialized
 */
public interface SerializationService<O> {
    String toJson(O object) throws SerializationException;

    String toJson(Iterable<O> objects) throws SerializationException;

    O toObject(String json) throws SerializationException;
    Set<O> toObjectSet(String json) throws SerializationException;
}
