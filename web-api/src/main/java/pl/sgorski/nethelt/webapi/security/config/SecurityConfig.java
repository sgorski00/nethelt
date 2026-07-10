package pl.sgorski.nethelt.webapi.security.config;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import pl.sgorski.nethelt.webapi.security.filter.JwtAuthenticationFilter;
import pl.sgorski.nethelt.webapi.security.oauth2.OAuth2AuthorizationRequestRepository;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

  @Value("${cors.allowed-origins}")
  private List<String> allowedOrigins;

  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final UserDetailsService userDetailsService;
  private final AccessDeniedHandler accessDeniedHandler;
  private final AuthenticationEntryPoint authenticationEntryPoint;
  private final AuthenticationSuccessHandler oauth2SuccessHandler;
  private final AuthenticationFailureHandler oauth2FailureHandler;
  private final OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService;
  private final OAuth2AuthorizationRequestRepository authorizationRequestRepository;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) {
    return http.authorizeHttpRequests(
            auth ->
                auth.requestMatchers(
                        "/actuator/**",
                        "/auth/refresh",
                        "/auth/logout",
                        "/oauth2/**",
                        "/login/oauth2/**")
                    .permitAll()
                    .requestMatchers("/auth/**")
                    .not()
                    .authenticated()
                    .requestMatchers("/profile/**", "/identities/**")
                    .authenticated()
                    .anyRequest()
                    .denyAll())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .requestCache(AbstractHttpConfigurer::disable)
        .oauth2Login(
            oauth ->
                oauth
                    .authorizationEndpoint(
                        auth -> auth.authorizationRequestRepository(authorizationRequestRepository))
                    .userInfoEndpoint(user -> user.userService(oauth2UserService))
                    .successHandler(oauth2SuccessHandler)
                    .failureHandler(oauth2FailureHandler))
        .userDetailsService(userDetailsService)
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .exceptionHandling(
            ex ->
                ex.accessDeniedHandler(accessDeniedHandler)
                    .authenticationEntryPoint(authenticationEntryPoint))
        .cors(cors -> cors.configurationSource(corsConfiguration()))
        .csrf(AbstractHttpConfigurer::disable)
        .build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager() {
    var authProvider = new DaoAuthenticationProvider(userDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder());
    return new ProviderManager(authProvider);
  }

  @Bean
  public CorsConfigurationSource corsConfiguration() {
    var corsConfiguration = new CorsConfiguration();
    corsConfiguration.setAllowedOrigins(allowedOrigins);
    corsConfiguration.addAllowedHeader("*");
    corsConfiguration.addAllowedMethod("*");
    corsConfiguration.setAllowCredentials(true);
    corsConfiguration.setMaxAge(3600L);
    var source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", corsConfiguration);
    return source;
  }
}
