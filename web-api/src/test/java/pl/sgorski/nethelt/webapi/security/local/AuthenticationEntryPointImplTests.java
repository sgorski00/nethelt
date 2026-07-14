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
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.servlet.HandlerExceptionResolver;

@ExtendWith(MockitoExtension.class)
public class AuthenticationEntryPointImplTests {

  @Mock private HandlerExceptionResolver resolver;
  @InjectMocks private AuthenticationEntryPointImpl authenticationEntryPoint;

  @Test
  void commence_shouldDelegateToResolver() throws ServletException, IOException {
    var req = new MockHttpServletRequest();
    var res = new MockHttpServletResponse();
    var ex = new TestAuthException();

    authenticationEntryPoint.commence(req, res, ex);

    verify(resolver).resolveException(req, res, null, ex);
  }

  private static class TestAuthException extends AuthenticationException {
    public TestAuthException() {
      super("Test authentication exception");
    }
  }
}
