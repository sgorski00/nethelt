package pl.sgorski.nethelt.agent.buffer;

import java.util.Collection;
import java.util.List;
import pl.sgorski.nethelt.agent.webclient.dto.request.MonitoringResultRequest;

/** Results that could not be sent to the Web API yet, oldest first. */
public interface ResultBuffer {

  /** Adds a result at the end, dropping the oldest ones when the buffer is full. */
  void add(MonitoringResultRequest request);

  /** Returns up to {@code limit} oldest results without removing them. */
  List<MonitoringResultRequest> peek(int limit);

  void remove(Collection<MonitoringResultRequest> requests);

  int size();
}
