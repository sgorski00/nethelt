package pl.sgorski.nethelt.agent.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Singleton ObjectMapper instance to be used across the application.
 */
public class ObjectMapperSingleton {

  /** ObjectMapper instance that should be used all over the app */
  private final static ObjectMapper INSTANCE;

  static {
    INSTANCE = createDefaultMapper();
  }

  private static ObjectMapper createDefaultMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());

    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    return mapper;
  }

  public static ObjectMapper getInstance() {
    return INSTANCE;
  }
}
