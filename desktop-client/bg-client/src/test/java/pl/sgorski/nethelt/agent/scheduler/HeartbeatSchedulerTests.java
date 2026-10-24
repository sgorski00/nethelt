package pl.sgorski.nethelt.agent.scheduler;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

import java.nio.file.AccessDeniedException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.sgorski.nethelt.agent.exception.CredentialsAccessDeniedException;
import pl.sgorski.nethelt.agent.exception.MissingCredentialsException;
import pl.sgorski.nethelt.agent.exception.WebClientException;
import pl.sgorski.nethelt.agent.webclient.api.web.AgentClient;

@ExtendWith(MockitoExtension.class)
public class HeartbeatSchedulerTests {

  @Mock private AgentClient agentClient;
  @InjectMocks private HeartbeatScheduler heartbeatScheduler;

  @Test
  void sendHeartbeat_shouldSendHeartbeat() {
    heartbeatScheduler.sendHeartbeat();

    verify(agentClient).heartbeat();
  }

  @Test
  void sendHeartbeat_shouldNotThrow_whenHeartbeatFails() {
    doThrow(new WebClientException("failed")).when(agentClient).heartbeat();

    assertDoesNotThrow(() -> heartbeatScheduler.sendHeartbeat());
  }

  @Test
  void sendHeartbeat_shouldNotThrow_whenCredentialsAreMissing() {
    doThrow(new MissingCredentialsException("no PAT")).when(agentClient).heartbeat();

    assertDoesNotThrow(() -> heartbeatScheduler.sendHeartbeat());
  }

  @Test
  void sendHeartbeat_shouldNotThrow_whenCredentialsAccessIsDenied() {
    var file = Path.of("/etc/nethelt/credentials");
    doThrow(new CredentialsAccessDeniedException(file, new AccessDeniedException(file.toString())))
        .when(agentClient)
        .heartbeat();

    assertDoesNotThrow(() -> heartbeatScheduler.sendHeartbeat());
  }
}
