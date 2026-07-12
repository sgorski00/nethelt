package pl.sgorski.nethelt.webapi.security.local;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import pl.sgorski.nethelt.webapi.features.auth.service.AccessTokenService;

@Component
@RequiredArgsConstructor
public final class AccessTokenAuthenticationFilter extends OncePerRequestFilter {

  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String BEARER_PREFIX = "Bearer ";

  private final AccessTokenService accessTokenService;
  private final UserDetailsService userDetailsService;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    var header = request.getHeader(AUTHORIZATION_HEADER);
    if (header == null || !header.startsWith(BEARER_PREFIX)) {
      filterChain.doFilter(request, response);
      return;
    }

    var token = header.substring(BEARER_PREFIX.length());
    if (!accessTokenService.isValid(token)) {
      filterChain.doFilter(request, response);
      return;
    }

    var email = accessTokenService.getEmailFromToken(token);
    var securityContext = SecurityContextHolder.getContext();
    if (securityContext.getAuthentication() == null) {
      var userDetails = userDetailsService.loadUserByUsername(email);
      var auth =
          new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
      auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
      securityContext.setAuthentication(auth);
    }
    filterChain.doFilter(request, response);
  }
}
