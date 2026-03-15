package pl.sgorski.nethelt.webapi.http.client.github.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.ImportHttpServices;
import pl.sgorski.nethelt.webapi.exception.http.client.GithubApiException;
import pl.sgorski.nethelt.webapi.http.client.github.GithubClient;

import java.nio.charset.StandardCharsets;

@Log4j2
@Configuration
@ImportHttpServices(group = "github", types = {GithubClient.class})
@RequiredArgsConstructor
public class GithubClientConfiguration {

    private static final String GITHUB_API_VERSION_KEY = "X-GitHub-Api-Version";
    private static final String GITHUB_RATE_LIMIT_REMAINING_KEY = "X-RateLimit-Remaining";
    private static final String GITHUB_RATE_LIMIT_RESET_KEY = "X-RateLimit-Reset";

    private final GithubClientProperties githubProperties;

    @Bean
    public RestClientHttpServiceGroupConfigurer groupConfigurer() {
        return groups -> groups
                .filterByName("github")
                .forEachClient((_, clientBuilder) -> clientBuilder
                        .baseUrl(githubProperties.baseUrl())
                        .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")
                        .defaultHeader(GITHUB_API_VERSION_KEY, "2026-03-10")
                        .requestInterceptor(githubErrorHandler())
                        .requestInterceptor(githubRateLimitHandler()));
    }

    private ClientHttpRequestInterceptor githubErrorHandler() {
        return (req, res, execution) -> {
            var response = execution.execute(req, res);
            var status = response.getStatusCode();
            if(status.isError()) {
                var body = "";
                try {
                    body = StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
                } catch (Exception e) {
                    body = "Something went wrong, but failed to read github response body";
                    log.error("Failed to read GitHub API error response body", e);
                }
                throw new GithubApiException("GitHub API error: " + body, status.value());
            }
            return response;
        };
    }

    private ClientHttpRequestInterceptor githubRateLimitHandler() {
        return (req, res, execution) -> {
            var response = execution.execute(req, res);
            var headers = response.getHeaders();
            var remaining = headers.getFirst(GITHUB_RATE_LIMIT_REMAINING_KEY);
            var reset = headers.getFirst(GITHUB_RATE_LIMIT_RESET_KEY);
            if(remaining != null && reset != null && Integer.parseInt(remaining) <= 10) {
                log.info("GitHub API rate limit remaining: {}, resets at: {}", remaining, reset);
            }
            return response;
        };
    }
}
