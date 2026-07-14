package pl.sgorski.nethelt.webapi.features.auth.scheduler;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.sgorski.nethelt.webapi.features.auth.service.RefreshTokenService;

@ExtendWith(MockitoExtension.class)
public class RefreshTokenCleanupJobTests {

  @Mock private RefreshTokenService refreshTokenService;
  @InjectMocks private RefreshTokenCleanupJob refreshTokenCleanupJob;

  @Test
  void task_shouldRunWithoutErrors() {
    assertDoesNotThrow(() -> refreshTokenCleanupJob.cleanUpInvalidTokens());
    verify(refreshTokenService).deleteInvalidTokens();
  }
}
