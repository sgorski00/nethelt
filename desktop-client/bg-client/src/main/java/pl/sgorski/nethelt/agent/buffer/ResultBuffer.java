package pl.sgorski.nethelt.agent.buffer;

import java.util.Collection;
import java.util.List;
import pl.sgorski.nethelt.agent.webclient.dto.request.MonitoringResultRequest;

public interface ResultBuffer {

  void add(MonitoringResultRequest request);

  List<MonitoringResultRequest> peek(int limit);

  void remove(Collection<MonitoringResultRequest> requests);

  int size();
}
