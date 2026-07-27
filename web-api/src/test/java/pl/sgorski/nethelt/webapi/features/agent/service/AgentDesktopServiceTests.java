package pl.sgorski.nethelt.webapi.features.agent.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.sgorski.nethelt.webapi.exception.domain.agent.AgentNotFoundException;
import pl.sgorski.nethelt.webapi.features.agent.repository.AgentRepository;
import pl.sgorski.nethelt.webapi.utils.TestAgentFactory;

@ExtendWith(MockitoExtension.class)
public class AgentDesktopServiceTests {

  @Mock private AgentRepository agentRepository;
  @InjectMocks private AgentDesktopService agentDesktopService;

  @Test
  void heartbeat_shouldThrow_whenAgentNotFound() {
    when(agentRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(AgentNotFoundException.class, () -> agentDesktopService.heartbeat(1L));
  }

  @Test
  void heartbeat_shouldUpdateHeartbeat_whenAgentFound() {
    var agent = TestAgentFactory.createAgent();
    when(agentRepository.findById(1L)).thenReturn(Optional.of(agent));

    agentDesktopService.heartbeat(1L);

    assertNotNull(agent.getLastHeartbeatAt());
  }
}
