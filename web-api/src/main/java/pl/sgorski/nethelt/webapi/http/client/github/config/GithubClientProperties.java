package pl.sgorski.nethelt.webapi.http.client.github.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "github.client")
public record GithubClientProperties(
        String baseUrl,
        Long readTimeoutMs,
        Long connectionTimeoutMs
) { }
