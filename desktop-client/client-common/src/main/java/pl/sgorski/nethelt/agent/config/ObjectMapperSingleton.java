package pl.sgorski.nethelt.agent.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Getter;

/**
 * Singleton ObjectMapper instance to be used across the application.
 */
@Getter
public enum ObjectMapperSingleton {
  INSTANCE;

  /** ObjectMapper instance that should be used all over the app */
  private final ObjectMapper mapper = createDefaultMapper();

  private static ObjectMapper createDefaultMapper() {
    var mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    return mapper;
  }
}
