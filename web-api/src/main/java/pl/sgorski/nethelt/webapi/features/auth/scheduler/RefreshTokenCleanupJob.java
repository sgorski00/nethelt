package pl.sgorski.nethelt.webapi.features.auth.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.sgorski.nethelt.webapi.features.auth.service.RefreshTokenService;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenCleanupJob {

  private final RefreshTokenService refreshTokenService;

  @Scheduled(cron = "0 0 */2 * * *")
  public void cleanUpInvalidTokens() {
    try {
      log.debug("Starting cleanup of expired and revoked refresh tokens...");
      refreshTokenService.deletedInvalidTokens();
      log.info("Refresh token cleanup completed successfully");
    } catch (Exception e) {
      log.error("Error during refresh token cleanup", e);
    }
  }
}
