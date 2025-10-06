package pl.sgorski.nethelt.agent.serialization;

import pl.sgorski.nethelt.core.model.Result;

/**
 * Service interface for serializing and deserializing Result objects to and from JSON.
 *
 * @param <R> the type of Result
 */
public interface ResultSerializationService<R extends Result> extends SerializationService<R> {
}
