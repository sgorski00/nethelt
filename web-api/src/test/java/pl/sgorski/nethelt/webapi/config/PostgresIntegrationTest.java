package pl.sgorski.nethelt.webapi.config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Base class for tests that need a real database. All subclasses share one PostgreSQL container,
 * started once per JVM, so the Spring context can be cached between test classes. The schema comes
 * from the Flyway migrations, which also checks that they match the entities.
 */
@SpringBootTest
@ActiveProfiles("test")
public abstract class PostgresIntegrationTest {

  @ServiceConnection
  static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:18.2-alpine");

  static {
    POSTGRES.start();
  }
}
