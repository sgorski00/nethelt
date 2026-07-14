package pl.sgorski.nethelt.webapi.security.local;

import static org.mockito.Mockito.verify;

import jakarta.servlet.ServletException;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.servlet.HandlerExceptionResolver;

@ExtendWith(MockitoExtension.class)
public class AccessDeniedHandlerImplTests {

  @Mock private HandlerExceptionResolver resolver;
  @InjectMocks private AccessDeniedHandlerImpl accessDeniedHandler;

  @Test
  void handle_shouldDelegateToResolver() throws ServletException, IOException {
    var req = new MockHttpServletRequest();
    var res = new MockHttpServletResponse();
    var ex = new AccessDeniedException("Access denied");

    accessDeniedHandler.handle(req, res, ex);

    verify(resolver).resolveException(req, res, null, ex);
  }
}
