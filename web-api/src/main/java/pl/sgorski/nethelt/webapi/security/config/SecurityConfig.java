package pl.sgorski.nethelt.webapi.security.config;

import lombok.RequiredArgsConstructor;
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
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import pl.sgorski.nethelt.webapi.security.filter.JwtAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;
    private final AccessDeniedHandler accessDeniedHandler;
    private final AuthenticationEntryPoint authenticationEntryPoint;
    private final AuthenticationSuccessHandler oauth2SuccessHandler;
    private final OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        return http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/auth/logout", "/auth/refresh").authenticated()
                        .requestMatchers("/auth/**").not().authenticated()
                        .requestMatchers("/oauth2/code/**", "/login/auth2/code/**").not().authenticated()
                        .requestMatchers("/profile/**").authenticated()
                        .anyRequest().denyAll())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.NEVER))
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(user -> user
                                .userService(oauth2UserService))
                        .successHandler(oauth2SuccessHandler))
                .userDetailsService(userDetailsService)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler(accessDeniedHandler)
                        .authenticationEntryPoint(authenticationEntryPoint))
                .cors(AbstractHttpConfigurer::disable)
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
}
